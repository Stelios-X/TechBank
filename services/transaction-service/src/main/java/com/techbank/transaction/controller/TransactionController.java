package com.techbank.transaction.controller;

import com.techbank.common.dto.TransactionDTO;
import com.techbank.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(
            @RequestParam String sourceAccount,
            @RequestParam String destinationAccount,
            @RequestParam BigDecimal amount,
            @RequestParam(defaultValue = "TRANSFER") String transactionType,
            @RequestParam(required = false) String description) {
        TransactionDTO transaction = transactionService.createTransaction(
                sourceAccount, destinationAccount, amount, transactionType, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransaction(@PathVariable Long id) {
        TransactionDTO transaction = transactionService.getTransaction(id);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/source/{accountNumber}")
    public ResponseEntity<Page<TransactionDTO>> getTransactionsBySource(
            @PathVariable String accountNumber,
            Pageable pageable) {
        Page<TransactionDTO> transactions = transactionService.getTransactionsBySourceAccount(accountNumber, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/destination/{accountNumber}")
    public ResponseEntity<Page<TransactionDTO>> getTransactionsByDestination(
            @PathVariable String accountNumber,
            Pageable pageable) {
        Page<TransactionDTO> transactions = transactionService.getTransactionsByDestinationAccount(accountNumber, pageable);
        return ResponseEntity.ok(transactions);
    }
}
