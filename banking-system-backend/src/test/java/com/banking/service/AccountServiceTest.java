package com.banking.service;

import com.banking.model.Account;
import com.banking.model.User;
import com.banking.repository.AccountRepository;
import com.banking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountService accountService;

    private User testUser;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Mary Anger");
        testUser.setEmail("mary@example.com");

        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setUser(testUser);
        testAccount.setBalance(new BigDecimal("5000.00"));
    }

    // ============================
    // CREATE ACCOUNT TESTS
    // ============================

    @Test
    @DisplayName("Create Account - Success")
    void createAccount_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        Account result = accountService.createAccount(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("Create Account - User Not Found")
    void createAccount_UserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> accountService.createAccount(99L));

        assertTrue(exception.getMessage().contains("User not found"));
        verify(accountRepository, never()).save(any(Account.class));
    }

    // ============================
    // FIND BY USER ID TESTS
    // ============================

    @Test
    @DisplayName("Find Accounts By User ID - Success")
    void findByUserId_Success() {
        Account account2 = new Account();
        account2.setId(2L);
        account2.setUser(testUser);
        account2.setBalance(new BigDecimal("3000.00"));

        when(accountRepository.findByUserId(1L)).thenReturn(Arrays.asList(testAccount, account2));

        List<Account> accounts = accountService.findByUserId(1L);

        assertEquals(2, accounts.size());
        assertEquals(new BigDecimal("5000.00"), accounts.get(0).getBalance());
        assertEquals(new BigDecimal("3000.00"), accounts.get(1).getBalance());
    }

    @Test
    @DisplayName("Find Accounts By User ID - No Accounts")
    void findByUserId_NoAccounts() {
        when(accountRepository.findByUserId(99L)).thenReturn(List.of());

        List<Account> accounts = accountService.findByUserId(99L);

        assertTrue(accounts.isEmpty());
    }

    // ============================
    // DEPOSIT TESTS
    // ============================

    @Test
    @DisplayName("Deposit - Success")
    void deposit_Success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArgument(0));

        Account result = accountService.deposit(1L, new BigDecimal("1000.00"));

        assertEquals(new BigDecimal("6000.00"), result.getBalance());
        verify(accountRepository).save(testAccount);
    }

    @Test
    @DisplayName("Deposit - Account Not Found")
    void deposit_AccountNotFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> accountService.deposit(99L, new BigDecimal("1000.00")));

        assertTrue(exception.getMessage().contains("Account not found"));
    }
}
