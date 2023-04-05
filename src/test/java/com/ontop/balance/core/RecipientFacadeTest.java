package com.ontop.balance.core;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.model.commands.CreateRecipientCommand;
import com.ontop.balance.core.model.queries.ObtainRecipientQuery;
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
    private RecipientFacade recipientFacade;

    @Test
    @DisplayName("""
        GIVEN a valid recipient creation request,
        WHEN the recipient creationg handler is invoked,
        THEN the recipient is successfully created""")
    void testHandlerCreateRecipientSuccessfull() {

        var command = new CreateRecipientCommand(1L, "John Doe", "123", "456", "789");

        doNothing().when(this.recipient).save(eq(command));

        this.recipientFacade.handler(command);

        verify(this.recipient).save(command);
    }

    @Test
    @DisplayName("""
        GIVEN a valid recipient obtain request,
        WHEN the recipient obtaining handler is invoked,
        THEN a list of recipients should be returned""")
    void testHandlerObtainRecipientSuccessfull() {

        var query = new ObtainRecipientQuery(5L);
        var recipientData = new RecipientData(5L, "John Doe", "123", "456", "789", BigDecimal.valueOf(0.1));

        doReturn(List.of(recipientData))
                .when(this.recipient).findRecipients(query.clientId());

        List<RecipientData> response = this.recipientFacade.handler(query);

        Assertions.assertThat(response).hasSizeGreaterThan(0);
    }
}
