package com.ontop.balance.core;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.ontop.balance.core.model.PaginatedWrapper;
import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.model.commands.CreateRecipientCommand;
import com.ontop.balance.core.model.queries.ObtainRecipientByClientQuery;
import com.ontop.balance.core.ports.outbound.Recipient;
import java.math.BigDecimal;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class RecipientFacadeTest extends BaseTestCase {

    @Mock
    private Recipient recipient;
    @InjectMocks
    private RecipientFacadeByClient recipientFacade;

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

        var query = new ObtainRecipientByClientQuery(5L);
        var recipientData = new RecipientData("$UUID", 5L, "John Doe", "123", "456", "789", BigDecimal.valueOf(0.1));

        PaginatedWrapper<RecipientData> wrapper = new PaginatedWrapper<>(
                List.of(recipientData), null);

        doReturn(wrapper).when(this.recipient).findRecipients(eq(query.clientId()));

        List<RecipientData> response = this.recipientFacade.handler(query).data();

        Assertions.assertThat(response).hasSizeGreaterThan(0);
    }
}
