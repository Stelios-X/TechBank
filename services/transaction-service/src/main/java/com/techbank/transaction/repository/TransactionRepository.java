package com.techbank.transaction.repository;

import com.techbank.transaction.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findBySourceAccount(String sourceAccount, Pageable pageable);
    Page<Transaction> findByDestinationAccount(String destinationAccount, Pageable pageable);
}
