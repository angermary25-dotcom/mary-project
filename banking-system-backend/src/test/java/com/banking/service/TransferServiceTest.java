package com.banking.service;

import com.banking.dto.TransferRequest;
import com.banking.dto.TransferResponse;
import com.banking.model.Account;
import com.banking.model.Transaction;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransferService transferService;

    private Account senderAccount;
    private Account receiverAccount;

    @BeforeEach
    void setUp() {
        senderAccount = new Account();
        senderAccount.setId(1L);
        senderAccount.setBalance(new BigDecimal("5000.00"));

        receiverAccount = new Account();
        receiverAccount.setId(2L);
        receiverAccount.setBalance(new BigDecimal("3000.00"));
    }

    // ============================
    // TRANSFER SUCCESS
    // ============================

    @Test
    @DisplayName("Transfer - Success")
    void transfer_Success() {
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("1000.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(receiverAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

        Transaction savedTransaction = new Transaction();
        savedTransaction.setId(1L);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        TransferResponse response = transferService.transfer(request);

        assertTrue(response.isSuccess());
        assertEquals("Transfer successful", response.getMessage());
        assertEquals(1L, response.getTransactionId());
        assertEquals(new BigDecimal("4000.00"), response.getSenderNewBalance());
        assertEquals(new BigDecimal("4000.00"), response.getReceiverNewBalance());

        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    // ============================
    // TRANSFER FAILURES
    // ============================

    @Test
    @DisplayName("Transfer - Sender Account Not Found")
    void transfer_SenderNotFound() {
        TransferRequest request = new TransferRequest(99L, 2L, new BigDecimal("1000.00"));

        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        TransferResponse response = transferService.transfer(request);

        assertFalse(response.isSuccess());
        assertEquals("Sender account not found", response.getMessage());

        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Transfer - Receiver Account Not Found")
    void transfer_ReceiverNotFound() {
        TransferRequest request = new TransferRequest(1L, 99L, new BigDecimal("1000.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        TransferResponse response = transferService.transfer(request);

        assertFalse(response.isSuccess());
        assertEquals("Receiver account not found", response.getMessage());

        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Transfer - Insufficient Funds")
    void transfer_InsufficientFunds() {
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("10000.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(receiverAccount));

        TransferResponse response = transferService.transfer(request);

        assertFalse(response.isSuccess());
        assertEquals("Insufficient funds", response.getMessage());
        assertEquals(new BigDecimal("5000.00"), response.getSenderNewBalance());

        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Transfer - Exact Balance (Edge Case)")
    void transfer_ExactBalance() {
        TransferRequest request = new TransferRequest(1L, 2L, new BigDecimal("5000.00"));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(senderAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(receiverAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

        Transaction savedTransaction = new Transaction();
        savedTransaction.setId(2L);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        TransferResponse response = transferService.transfer(request);

        assertTrue(response.isSuccess());
        assertEquals(BigDecimal.ZERO.setScale(2), response.getSenderNewBalance().setScale(2));
        assertEquals(new BigDecimal("8000.00"), response.getReceiverNewBalance());
    }
}
