package com.banking.controller;

import com.banking.dto.RegisterRequest;
import com.banking.dto.TransferRequest;
import com.banking.model.Account;
import com.banking.model.User;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import com.banking.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransferControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String jwtToken;
    private Long senderAccountId;
    private Long receiverAccountId;

    @BeforeEach
    void setUp() throws Exception {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();

        // Create sender user + account
        User sender = new User();
        sender.setName("Mary Anger");
        sender.setEmail("mary@example.com");
        sender.setPassword(passwordEncoder.encode("123456"));
        sender.setRole("USER");
        sender = userRepository.save(sender);

        Account senderAcc = new Account();
        senderAcc.setUser(sender);
        senderAcc.setBalance(new BigDecimal("5000.00"));
        senderAcc = accountRepository.save(senderAcc);
        senderAccountId = senderAcc.getId();

        // Create receiver user + account
        User receiver = new User();
        receiver.setName("John Doe");
        receiver.setEmail("john@example.com");
        receiver.setPassword(passwordEncoder.encode("123456"));
        receiver.setRole("USER");
        receiver = userRepository.save(receiver);

        Account receiverAcc = new Account();
        receiverAcc.setUser(receiver);
        receiverAcc.setBalance(new BigDecimal("3000.00"));
        receiverAcc = accountRepository.save(receiverAcc);
        receiverAccountId = receiverAcc.getId();

        // Login to get JWT token
        String loginJson = objectMapper.writeValueAsString(
                new com.banking.dto.LoginRequest("mary@example.com", "123456"));

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = loginResult.getResponse().getContentAsString();
        jwtToken = objectMapper.readTree(responseBody).get("token").asText();
    }

    // ============================
    // TRANSFER TESTS
    // ============================

    @Test
    @DisplayName("POST /api/transfer - Success")
    void transfer_Success() throws Exception {
        TransferRequest request = new TransferRequest(senderAccountId, receiverAccountId, new BigDecimal("1000.00"));

        mockMvc.perform(post("/api/transfer")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Transfer successful"))
                .andExpect(jsonPath("$.transactionId").isNumber())
                .andExpect(jsonPath("$.senderNewBalance").value(4000.00))
                .andExpect(jsonPath("$.receiverNewBalance").value(4000.00));
    }

    @Test
    @DisplayName("POST /api/transfer - Insufficient Funds")
    void transfer_InsufficientFunds() throws Exception {
        TransferRequest request = new TransferRequest(senderAccountId, receiverAccountId, new BigDecimal("99999.00"));

        mockMvc.perform(post("/api/transfer")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Insufficient funds"));
    }

    @Test
    @DisplayName("POST /api/transfer - No Auth Token (401)")
    void transfer_NoAuthToken() throws Exception {
        TransferRequest request = new TransferRequest(senderAccountId, receiverAccountId, new BigDecimal("100.00"));

        mockMvc.perform(post("/api/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/transfer - Invalid Account")
    void transfer_InvalidAccount() throws Exception {
        TransferRequest request = new TransferRequest(999L, receiverAccountId, new BigDecimal("100.00"));

        mockMvc.perform(post("/api/transfer")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Sender account not found"));
    }
}
