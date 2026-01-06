package com.ontop.balance.app.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ontop.balance.app.models.ErrorResponse;
import com.ontop.balance.infrastructure.entities.ClientCredentialsEntity;
import com.ontop.balance.infrastructure.repositories.ClientCredentialsRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration tests for pagination parameter validation at the HTTP level.
 * <p>
 * These tests verify that invalid pagination parameters (page, size) are properly
 * validated and return HTTP 400 BAD_REQUEST with appropriate error messages.
 * Unlike unit tests that validate record instances directly, these tests exercise
 * the full controller layer including @Validated annotation on controller methods.
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Pagination Validation Integration Tests")
class PaginationValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientCredentialsRepository clientCredentialsRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String validJwtToken;
    private static final String TEST_SECRET = "test_secret_123";

    @BeforeEach
    void setUp() {
        // Clean up database
        clientCredentialsRepository.deleteAll();

        // Create test client
        ClientCredentialsEntity client = new ClientCredentialsEntity();
        client.setClientId(1L);
        client.setSecretHash(passwordEncoder.encode(TEST_SECRET));
        client.setActive(true);
        client.setCreatedAt(LocalDateTime.now());
        clientCredentialsRepository.save(client);

        // Generate valid JWT token
        validJwtToken = generateJwtToken(1L);
    }

    private String generateJwtToken(Long clientId) {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        Key key = Keys.hmacShaKeyFor(keyBytes);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationDate = now.plusHours(24);
        Date issuedAt = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        Date expiresAt = Date.from(expirationDate.atZone(ZoneId.systemDefault()).toInstant());

        return Jwts.builder()
                .setSubject(String.valueOf(clientId))
                .setIssuedAt(issuedAt)
                .setExpiration(expiresAt)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ==================== Transactions Endpoint Tests ====================

    @Test
    @DisplayName("GET /transactions with page=-1 should return 400")
    void testTransactionsEndpointWithNegativePageReturns400() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/transactions")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .header("X-Client-Id", "1")
                        .param("page", "-1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertEquals(400, status,
                "HTTP status should be 400 BAD_REQUEST for negative page parameter");

        String response = result.getResponse().getContentAsString();
        ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);
        assertNotNull(errorResponse, "Error response should not be null");
        assertEquals("Validation failed!", errorResponse.message());
        assertTrue(errorResponse.subErrors().size() > 0,
                "Should contain validation errors");
    }

    @Test
    @DisplayName("GET /transactions with size=0 should return 400")
    void testTransactionsEndpointWithZeroSizeReturns400() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/transactions")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .header("X-Client-Id", "1")
                        .param("page", "0")
                        .param("size", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertEquals(400, status,
                "HTTP status should be 400 BAD_REQUEST for zero size parameter");

        String response = result.getResponse().getContentAsString();
        ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);
        assertNotNull(errorResponse, "Error response should not be null");
        assertEquals("Validation failed!", errorResponse.message());
        assertTrue(errorResponse.subErrors().size() > 0,
                "Should contain validation errors");
    }

    @Test
    @DisplayName("GET /transactions with size=101 should return 400")
    void testTransactionsEndpointWithExcessiveSizeReturns400() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/transactions")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .header("X-Client-Id", "1")
                        .param("page", "0")
                        .param("size", "101")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertEquals(400, status,
                "HTTP status should be 400 BAD_REQUEST for size > 100");

        String response = result.getResponse().getContentAsString();
        ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);
        assertNotNull(errorResponse, "Error response should not be null");
        assertEquals("Validation failed!", errorResponse.message());
        assertTrue(errorResponse.subErrors().size() > 0,
                "Should contain validation errors");
    }

    @Test
    @DisplayName("GET /transactions with multiple invalid params should return 400")
    void testTransactionsEndpointWithMultipleInvalidParamsReturns400() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/transactions")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .header("X-Client-Id", "1")
                        .param("page", "-1")
                        .param("size", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertEquals(400, status,
                "HTTP status should be 400 BAD_REQUEST for multiple invalid parameters");

        String response = result.getResponse().getContentAsString();
        ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);
        assertNotNull(errorResponse, "Error response should not be null");
        assertEquals("Validation failed!", errorResponse.message());
        assertTrue(errorResponse.subErrors().size() >= 2,
                "Should contain at least 2 validation errors");
    }

    @Test
    @DisplayName("GET /transactions with valid params should not return 400")
    void testTransactionsEndpointWithValidParamsReturnsNot400() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/transactions")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .header("X-Client-Id", "1")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        int status = result.getResponse().getStatus();
        // Should return 200 OK or any status except 400
        // (may return 200 with empty list, or other status depending on data)
        // The important thing is it should NOT be 400 validation error
        assertTrue(status != 400,
                "HTTP status should NOT be 400 for valid parameters (actual: " + status + ")");
    }

    // ==================== Recipients Endpoint Tests ====================

    @Test
    @DisplayName("GET /recipients with page=-1 should return 400")
    void testRecipientsEndpointWithNegativePageReturns400() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/recipients")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .header("X-Client-Id", "1")
                        .param("page", "-1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertEquals(400, status,
                "HTTP status should be 400 BAD_REQUEST for negative page parameter");

        String response = result.getResponse().getContentAsString();
        ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);
        assertNotNull(errorResponse, "Error response should not be null");
        assertEquals("Validation failed!", errorResponse.message());
        assertTrue(errorResponse.subErrors().size() > 0,
                "Should contain validation errors");
    }

    @Test
    @DisplayName("GET /recipients with size=0 should return 400")
    void testRecipientsEndpointWithZeroSizeReturns400() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/recipients")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .header("X-Client-Id", "1")
                        .param("page", "0")
                        .param("size", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertEquals(400, status,
                "HTTP status should be 400 BAD_REQUEST for zero size parameter");

        String response = result.getResponse().getContentAsString();
        ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);
        assertNotNull(errorResponse, "Error response should not be null");
        assertEquals("Validation failed!", errorResponse.message());
        assertTrue(errorResponse.subErrors().size() > 0,
                "Should contain validation errors");
    }

    @Test
    @DisplayName("GET /recipients with size=101 should return 400")
    void testRecipientsEndpointWithExcessiveSizeReturns400() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/recipients")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .header("X-Client-Id", "1")
                        .param("page", "0")
                        .param("size", "101")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertEquals(400, status,
                "HTTP status should be 400 BAD_REQUEST for size > 100");

        String response = result.getResponse().getContentAsString();
        ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);
        assertNotNull(errorResponse, "Error response should not be null");
        assertEquals("Validation failed!", errorResponse.message());
        assertTrue(errorResponse.subErrors().size() > 0,
                "Should contain validation errors");
    }

    @Test
    @DisplayName("GET /recipients with multiple invalid params should return 400")
    void testRecipientsEndpointWithMultipleInvalidParamsReturns400() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/recipients")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .header("X-Client-Id", "1")
                        .param("page", "-5")
                        .param("size", "200")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        int status = result.getResponse().getStatus();
        assertEquals(400, status,
                "HTTP status should be 400 BAD_REQUEST for multiple invalid parameters");

        String response = result.getResponse().getContentAsString();
        ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);
        assertNotNull(errorResponse, "Error response should not be null");
        assertEquals("Validation failed!", errorResponse.message());
        assertTrue(errorResponse.subErrors().size() >= 2,
                "Should contain at least 2 validation errors");
    }

    @Test
    @DisplayName("GET /recipients with valid params should not return 400")
    void testRecipientsEndpointWithValidParamsReturnsNot400() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/recipients")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .header("X-Client-Id", "1")
                        .param("page", "0")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        int status = result.getResponse().getStatus();
        // Should return 200 OK or any status except 400
        // The important thing is it should NOT be 400 validation error
        assertTrue(status != 400,
                "HTTP status should NOT be 400 for valid parameters (actual: " + status + ")");
    }

    @Test
    @DisplayName("GET /recipients with default params should not return 400")
    void testRecipientsEndpointWithDefaultParamsReturnsNot400() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/recipients")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .header("X-Client-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        int status = result.getResponse().getStatus();
        // Should return 200 OK or any status except 400
        // The important thing is it should NOT be 400 validation error
        assertTrue(status != 400,
                "HTTP status should NOT be 400 for default parameters (actual: " + status + ")");
    }

    // ==================== Error Response Structure Tests ====================

    @Test
    @DisplayName("Error response should contain proper structure for invalid page")
    void testErrorResponseStructureForInvalidPage() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/transactions")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .header("X-Client-Id", "1")
                        .param("page", "-1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);

        assertNotNull(errorResponse.message(), "Error response should have message");
        assertNotNull(errorResponse.subErrors(), "Error response should have sub-errors");
        assertTrue(errorResponse.subErrors().size() > 0, "Should have at least one sub-error");

        ErrorResponse.SubErrorResponse subError = errorResponse.subErrors().get(0);
        assertNotNull(subError.key(), "Sub-error should have key");
        assertNotNull(subError.message(), "Sub-error should have message");
    }

    @Test
    @DisplayName("Error response message should be 'Validation failed!'")
    void testErrorResponseMessage() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/recipients")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .header("X-Client-Id", "1")
                        .param("page", "-1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        ErrorResponse errorResponse = objectMapper.readValue(response, ErrorResponse.class);

        assertEquals("Validation failed!", errorResponse.message(),
                "Error message should be 'Validation failed!'");
    }
}
