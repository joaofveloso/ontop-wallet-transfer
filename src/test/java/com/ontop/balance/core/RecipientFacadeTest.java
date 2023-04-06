package com.ontop.balance.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.ontop.balance.core.model.PaginatedWrapper;
import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.model.commands.CreateRecipientCommand;
import com.ontop.balance.core.model.exceptions.UnauthorizedAccessToResourceException;
import com.ontop.balance.core.model.queries.ObtainRecipientByClientQuery;
import com.ontop.balance.core.model.queries.ObtainRecipientByIdQuery;
import com.ontop.balance.core.ports.outbound.Recipient;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class RecipientFacadeTest extends BaseTestCase {

    @Mock
    private Recipient recipient;
    @InjectMocks
    private RecipientFacade recipientFacade;

    @Test
    @DisplayName("""
        GIVEN a valid recipient creation request,
        WHEN the recipient creationg handler is invoked,
        THEN the recipient is successfully created""")
    void testHandlerCreateRecipientSuccessfull() {

        var command = new CreateRecipientCommand(1L, "John Doe", "123", "456", "789");

        doNothing().when(this.recipient).save(eq(command), anyString());

        this.recipientFacade.handler(command);

        verify(this.recipient).save(eq(command), anyString());
    }

    @Test
    @DisplayName("""
        GIVEN a valid recipient obtain request,
        WHEN the recipient obtaining handler is invoked,
        THEN a list of recipients should be returned""")
    void testHandlerObtainRecipientSuccessfull() {

        var query = new ObtainRecipientByClientQuery(5L, 0, 5);
        var recipientData = new RecipientData(
                "$UUID", 5L, "John Doe", "123", "456", "789", BigDecimal.valueOf(0.1));

        PaginatedWrapper<RecipientData> wrapper = new PaginatedWrapper<>(
                List.of(recipientData), null);

        doReturn(wrapper).when(this.recipient).findRecipients(eq(query));

        List<RecipientData> response = this.recipientFacade.handler(query).data();

        assertThat(response).hasSizeGreaterThan(0);
        verify(this.recipient).findRecipients(eq(query));
    }

    @Test
    @DisplayName("""
            GIVEN a valid recipient request by id
            WHEN the recipient by Id is invoked
            THEN the recipient should be returned""")
    void testHandlerObtainRecipitentByIdSuccessfull(){
        String uuid = UUID.randomUUID().toString();
        var query = new ObtainRecipientByIdQuery(uuid, 1L);
        var recipientData = new RecipientData(
                uuid, 1L,"John Doe","123456", "456", "789", BigDecimal.valueOf(0.1));

        doReturn(Optional.of(recipientData)).when(this.recipient).findRecipientById(eq(query));

        RecipientData recipientDataResponse = this.recipientFacade.handler(query);

        assertThat(recipientDataResponse).isEqualTo(recipientData);
        verify(this.recipient).findRecipientById(eq(query));
    }

    @Test
    @DisplayName("""
            GIVEN a recipient request with an id from different ownership
            WHEN the recipient by Id is invoked
            THEN and UnauthorizedAccessToResourceException should be throw""")
    void testHandlerObtainRecipitentWithIdFromDifferentOwnershipFailed() {

        String uuid = UUID.randomUUID().toString();
        var query = new ObtainRecipientByIdQuery(uuid, 1L);
        var recipientData = new RecipientData(
                uuid, 999L,"John Doe","123456", "456", "789", BigDecimal.valueOf(0.1));

        doReturn(Optional.of(recipientData)).when(this.recipient).findRecipientById(eq(query));

        assertThrows(UnauthorizedAccessToResourceException.class,
                () -> this.recipientFacade.handler(query));

        verify(this.recipient).findRecipientById(eq(query));
    }
}
