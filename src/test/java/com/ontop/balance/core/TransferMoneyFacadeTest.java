package com.ontop.balance.core;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ontop.balance.core.model.BalanceData;
import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.model.TransactionData.TransactionStatus;
import com.ontop.balance.core.model.commands.TransferMoneyCommand;
import com.ontop.balance.core.model.exceptions.IllegalAmountValueExcpetion;
import com.ontop.balance.core.model.exceptions.InsufficientBalanceException;
import com.ontop.balance.core.model.exceptions.InvalidFeeException;
import com.ontop.balance.core.model.exceptions.OwnershipValidationException;
import com.ontop.balance.core.model.exceptions.RecipientNotFoundException;
import com.ontop.balance.core.model.exceptions.WalletNotFoundException;
import com.ontop.balance.core.model.queries.ObtainRecipientByIdQuery;
import com.ontop.balance.core.ports.outbound.Payment;
import com.ontop.balance.core.ports.outbound.Recipient;
import com.ontop.balance.core.ports.outbound.Transaction;
import com.ontop.balance.core.ports.outbound.Wallet;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class TransferMoneyFacadeTest extends BaseTestCase {

    @Mock
    private Recipient recipient;
    @Mock
    private Wallet wallet;
    @Mock
    private Payment payment;
    @Mock
    private Transaction transaction;
    @InjectMocks
    private TransferMoneyFacade transferMoneyFacade;

    @Test
    @DisplayName("""
            GIVEN a valid money transfer request,
             WHEN the handler is invoked,
             THEN the wallet amount must be withdrawn, and payment must be executed with amount deducted
             by the fee""")
    void testHandlerSuccessfullTransaction() {

        String uuid = UUID.randomUUID().toString();

        var command = new TransferMoneyCommand(uuid, 1L, BigDecimal.valueOf(1_000));
        var recipientData = new RecipientData(uuid, 1L, "John Doe", "123", "456", "789",
                BigDecimal.valueOf(0.1));
        var balanceData = new BalanceData(BigDecimal.valueOf(10_000));
        var recipientQuery = new ObtainRecipientByIdQuery(command.recipientId(),
                command.clientId());

        doReturn(Optional.of(recipientData)).when(this.recipient)
                .findRecipientById(any(ObtainRecipientByIdQuery.class));
        doReturn(Optional.of(balanceData)).when(this.wallet).getBalance(anyLong());
        doReturn(TransactionStatus.PENDING).when(this.wallet)
                .prepareWithdraw(any(BigDecimal.class), any(RecipientData.class), anyString());
        doReturn(TransactionStatus.PENDING).when(this.payment)
                .prepareTransfer(any(BigDecimal.class), any(RecipientData.class), anyString());

        this.transferMoneyFacade.handler(command);

        verify(this.recipient).findRecipientById(eq(recipientQuery));
        verify(this.wallet).getBalance(eq(recipientData.clientId()));
        verify(this.wallet).prepareWithdraw(eq(command.amount()), eq(recipientData), anyString());
        verify(this.payment).prepareTransfer(eq(recipientData.applyFee(command.amount())),
                eq(recipientData), anyString());
    }

    @Test
    @DisplayName("""
            GIVEN a money transfer request with an invalid 'RecipientId',
            WHEN the handler is invoked,
            THEN a RecipientNotFoundException should be thrown""")
    void testHandlerRecipientNotFound() {

        String uuid = UUID.randomUUID().toString();
        var command = new TransferMoneyCommand(uuid, 1L, BigDecimal.valueOf(1_000));
        var recipientQuery = new ObtainRecipientByIdQuery(command.recipientId(),
                command.clientId());

        doThrow(new RecipientNotFoundException()).when(this.recipient)
                .findRecipientById(any(ObtainRecipientByIdQuery.class));

        assertThrows(RecipientNotFoundException.class,
                () -> this.transferMoneyFacade.handler(command));

        verify(this.recipient).findRecipientById(eq(recipientQuery));

        verify(this.wallet, never()).getBalance(anyLong());
        verify(this.wallet, never()).prepareWithdraw(any(BigDecimal.class),
                any(RecipientData.class), anyString());
        verify(this.payment, never()).prepareTransfer(any(BigDecimal.class),
                any(RecipientData.class), anyString());
    }

    @Test
    @DisplayName("""
            GIVEN a money transfer request with a client without wallet,
            WHEN the handler is invoked,
            THEN a WalletNotFoundException should be thrown""")
    void testHandlerWalletNotFound() {
        String uuid = UUID.randomUUID().toString();
        var command = new TransferMoneyCommand(uuid, 1L, BigDecimal.valueOf(1_000));
        var recipientData = new RecipientData(uuid, 1L, "John Doe", "123", "456", "789",
                BigDecimal.valueOf(0.1));
        var recipientQuery = new ObtainRecipientByIdQuery(command.recipientId(),
                command.clientId());

        doReturn(Optional.of(recipientData)).when(this.recipient)
                .findRecipientById(any(ObtainRecipientByIdQuery.class));
        doThrow(new WalletNotFoundException()).when(this.wallet).getBalance(anyLong());

        assertThrows(WalletNotFoundException.class,
                () -> this.transferMoneyFacade.handler(command));

        verify(this.recipient).findRecipientById(eq(recipientQuery));
        verify(this.wallet).getBalance(eq(recipientData.clientId()));
        verify(this.wallet, never()).prepareWithdraw(any(BigDecimal.class),
                any(RecipientData.class), anyString());
        verify(this.payment, never()).prepareTransfer(any(BigDecimal.class),
                any(RecipientData.class), anyString());
    }

    @Test
    @DisplayName("""
            GIVEN a money transfer request with amount hihger than balance,
            WHEN the handler is invoked,
            THEN a InsufficientBalanceException should be thrown""")
    void testHandlerInsuffiecientBalance() {
        String uuid = UUID.randomUUID().toString();
        var command = new TransferMoneyCommand(uuid, 1L, BigDecimal.valueOf(5_000));
        var recipientData = new RecipientData(uuid, 1L, "John Doe", "123", "456", "789",
                BigDecimal.valueOf(0.1));
        var balanceData = new BalanceData(BigDecimal.valueOf(1_000));
        var recipientQuery = new ObtainRecipientByIdQuery(command.recipientId(),
                command.clientId());

        doReturn(Optional.of(recipientData)).when(this.recipient)
                .findRecipientById(any(ObtainRecipientByIdQuery.class));
        doReturn(Optional.of(balanceData)).when(this.wallet).getBalance(anyLong());

        assertThrows(InsufficientBalanceException.class,
                () -> this.transferMoneyFacade.handler(command));

        verify(this.recipient).findRecipientById(eq(recipientQuery));
        verify(this.wallet).getBalance(eq(recipientData.clientId()));
        verify(this.wallet, never()).prepareWithdraw(any(BigDecimal.class),
                any(RecipientData.class), anyString());
        verify(this.payment, never()).prepareTransfer(any(BigDecimal.class),
                any(RecipientData.class), anyString());
    }

    @Test
    @DisplayName("""
            GIVEN an invalid fee value is define in the appication,
            WHEN the Fee instance is created,
            THEN an InvalidFeeException should be thrown""")
    void testHandlerInvalidFee() {

        String uuid = UUID.randomUUID().toString();

        assertThrows(InvalidFeeException.class,
                () -> new RecipientData(uuid, 1L, "John Doe", "123", "456", "789",
                        BigDecimal.valueOf(1.01)));
    }

    @Test
    @DisplayName("""
            GIVEN a money transfer request amout value defined as NULL,
            WHEN the handler is invoked,
            THEN a IllegalAmountValueExcpetion should be thrown""")
    void testHandlerInvalidAmountForNullValue() {

        String uuid = UUID.randomUUID().toString();
        var command = new TransferMoneyCommand(uuid, 5L, null);
        var recipientData = new RecipientData(uuid, 5L, "John Doe", "123", "456", "789",
                BigDecimal.valueOf(0.1));
        var recipientQuery = new ObtainRecipientByIdQuery(command.recipientId(),
                command.clientId());

        doReturn(Optional.of(recipientData)).when(this.recipient)
                .findRecipientById(any(ObtainRecipientByIdQuery.class));

        assertThrows(IllegalAmountValueExcpetion.class,
                () -> this.transferMoneyFacade.handler(command));

        verify(this.recipient).findRecipientById(eq(recipientQuery));
        verify(this.wallet, never()).getBalance(anyLong());
        verify(this.wallet, never()).prepareWithdraw(any(BigDecimal.class),
                any(RecipientData.class), anyString());
        verify(this.payment, never()).prepareTransfer(any(BigDecimal.class),
                any(RecipientData.class), anyString());
    }

    @Test
    @DisplayName("""
            GIVEN a money transfer request amout value defined as negative,
            WHEN the handler is invoked,
            THEN a IllegalAmountValueExcpetion should be thrown""")
    void testHandlerInvalidAmountForNegativeValue() {
        String uuid = UUID.randomUUID().toString();
        var command = new TransferMoneyCommand(uuid, 5L, BigDecimal.valueOf(-100));
        var recipientData = new RecipientData(uuid, 5L, "John Doe", "123", "456", "789",
                BigDecimal.valueOf(0.1));
        var recipientQuery = new ObtainRecipientByIdQuery(command.recipientId(),
                command.clientId());

        doReturn(Optional.of(recipientData)).when(this.recipient)
                .findRecipientById(any(ObtainRecipientByIdQuery.class));

        assertThrows(IllegalAmountValueExcpetion.class,
                () -> this.transferMoneyFacade.handler(command));

        verify(this.recipient).findRecipientById(eq(recipientQuery));
        verify(this.wallet, never()).getBalance(anyLong());
        verify(this.wallet, never()).prepareWithdraw(any(BigDecimal.class),
                any(RecipientData.class), anyString());
        verify(this.payment, never()).prepareTransfer(any(BigDecimal.class),
                any(RecipientData.class), anyString());
    }

    @Test
    @DisplayName("""
            GIVEN a money transfer request to a recipient from another client,
            WHEN the handler is invoked,
            THEN a IllegalAmountValueExcpetion should be thrown""")
    void testHandlerUnauthorizedAccessToRecipient() {

        String uuid = UUID.randomUUID().toString();
        var command = new TransferMoneyCommand(uuid, 999L, BigDecimal.valueOf(1_000));
        var recipientData = new RecipientData(uuid, 1L, "John Doe", "123", "456", "789",
                BigDecimal.valueOf(0.1));
        var recipientQuery = new ObtainRecipientByIdQuery(command.recipientId(),
                command.clientId());

        doReturn(Optional.of(recipientData)).when(this.recipient)
                .findRecipientById(any(ObtainRecipientByIdQuery.class));

        assertThrows(OwnershipValidationException.class,
                () -> this.transferMoneyFacade.handler(command));

        verify(this.recipient).findRecipientById(eq(recipientQuery));
        verify(this.wallet, never()).getBalance(anyLong());
        verify(this.wallet, never()).prepareWithdraw(any(BigDecimal.class),
                any(RecipientData.class), anyString());
        verify(this.payment, never()).prepareTransfer(any(BigDecimal.class),
                any(RecipientData.class), anyString());
    }
}
