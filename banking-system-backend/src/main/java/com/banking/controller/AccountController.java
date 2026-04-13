package com.banking.controller;

import com.banking.dto.AccountResponse;
import com.banking.model.Account;
import com.banking.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/create/{userId}")
    public ResponseEntity<Account> createAccount(@PathVariable Long userId) {
        return ResponseEntity.ok(accountService.createAccount(userId));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<AccountResponse>> getAccountsByUserId(@PathVariable Long userId) {
        List<Account> accounts = accountService.findByUserId(userId);
        List<AccountResponse> response = accounts.stream()
                .map(acc -> new AccountResponse(acc.getId(), acc.getBalance()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<Account> deposit(@PathVariable Long id, @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(accountService.deposit(id, amount));
    }
}
