package com.techbank.transaction.service;

import com.techbank.common.dto.TransactionDTO;
import com.techbank.transaction.entity.Transaction;
import com.techbank.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionService {
    private final TransactionRepository transactionRepository;

    public TransactionDTO createTransaction(String sourceAccount, String destinationAccount, 
                                           BigDecimal amount, String transactionType, String description) {
        log.info("Creating transaction from {} to {} for amount {}", sourceAccount, destinationAccount, amount);
        
        Transaction transaction = Transaction.builder()
                .sourceAccount(sourceAccount)
                .destinationAccount(destinationAccount)
                .amount(amount)
                .transactionType(transactionType)
                .status("COMPLETED")
                .description(description)
                .build();

        Transaction savedTransaction = transactionRepository.save(transaction);
        return toDTO(savedTransaction);
    }

    public TransactionDTO getTransaction(Long id) {
        log.info("Fetching transaction: {}", id);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + id));
        return toDTO(transaction);
    }

    public Page<TransactionDTO> getTransactionsBySourceAccount(String sourceAccount, Pageable pageable) {
        log.info("Fetching transactions for source account: {}", sourceAccount);
        return transactionRepository.findBySourceAccount(sourceAccount, pageable)
                .map(this::toDTO);
    }

    public Page<TransactionDTO> getTransactionsByDestinationAccount(String destinationAccount, Pageable pageable) {
        log.info("Fetching transactions for destination account: {}", destinationAccount);
        return transactionRepository.findByDestinationAccount(destinationAccount, pageable)
                .map(this::toDTO);
    }

    private TransactionDTO toDTO(Transaction transaction) {
        return TransactionDTO.builder()
                .transactionId(transaction.getId())
                .sourceAccount(transaction.getSourceAccount())
                .destinationAccount(transaction.getDestinationAccount())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType())
                .status(transaction.getStatus())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
