package com.ontop.balance.infrastructure.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientCredentialsEntityTest {

    @Test
    void testEntityCreation() {
        ClientCredentialsEntity entity = new ClientCredentialsEntity();
        entity.setClientId(123456L);
        entity.setSecretHash("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy");
        entity.setActive(true);

        assertEquals(123456L, entity.getClientId());
        assertEquals("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy", entity.getSecretHash());
        assertTrue(entity.isActive());
        assertNotNull(entity.getCreatedAt());
    }

    @Test
    void testEntityDefaultsToActive() {
        ClientCredentialsEntity entity = new ClientCredentialsEntity();
        assertTrue(entity.isActive());
    }
}
