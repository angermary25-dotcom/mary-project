package com.banking.service;

import com.banking.dto.TransferRequest;
import com.banking.dto.TransferResponse;
import com.banking.model.Account;
import com.banking.model.Transaction;
import com.banking.repository.AccountRepository;
import com.banking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        // Step 1: Check sender account exists
        Account sender = accountRepository.findById(request.getSenderAccountId())
                .orElse(null);
        if (sender == null) {
            return new TransferResponse(false, "Sender account not found", null, null, null);
        }

        // Step 2: Check receiver account exists
        Account receiver = accountRepository.findById(request.getReceiverAccountId())
                .orElse(null);
        if (receiver == null) {
            return new TransferResponse(false, "Receiver account not found", null, null, null);
        }

        // Step 3: Check sender has enough balance
        if (sender.getBalance().compareTo(request.getAmount()) < 0) {
            return new TransferResponse(false, "Insufficient funds", null, sender.getBalance(), null);
        }

        // Step 4: Deduct amount from sender
        sender.setBalance(sender.getBalance().subtract(request.getAmount()));
        accountRepository.save(sender);

        // Step 5: Add amount to receiver
        receiver.setBalance(receiver.getBalance().add(request.getAmount()));
        accountRepository.save(receiver);

        // Step 6: Save transaction
        Transaction transaction = new Transaction();
        transaction.setSenderAccount(sender);
        transaction.setReceiverAccount(receiver);
        transaction.setAmount(request.getAmount());
        transaction.setTimestamp(LocalDateTime.now());
        transaction.setStatus("SUCCESS");
        Transaction savedTransaction = transactionRepository.save(transaction);

        return new TransferResponse(
                true,
                "Transfer successful",
                savedTransaction.getId(),
                sender.getBalance(),
                receiver.getBalance()
        );
    }
}
