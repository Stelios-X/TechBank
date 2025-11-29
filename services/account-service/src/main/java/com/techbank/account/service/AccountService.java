package com.techbank.account.service;

import com.techbank.account.entity.Account;
import com.techbank.account.repository.AccountRepository;
import com.techbank.common.dto.AccountDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountDTO createAccount(String accountNumber, String accountHolder) {
        log.info("Creating account: {} for {}", accountNumber, accountHolder);
        
        Account account = Account.builder()
                .accountNumber(accountNumber)
                .accountHolder(accountHolder)
                .balance(BigDecimal.ZERO)
                .status("ACTIVE")
                .build();

        Account savedAccount = accountRepository.save(account);
        return toDTO(savedAccount);
    }

    public AccountDTO getAccount(String accountNumber) {
        log.info("Fetching account: {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));
        return toDTO(account);
    }

    public List<AccountDTO> getAllAccounts() {
        log.info("Fetching all accounts");
        return accountRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    public AccountDTO deposit(String accountNumber, BigDecimal amount) {
        log.info("Depositing {} to account {}", amount, accountNumber);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Deposit amount must be positive");
        }

        account.setBalance(account.getBalance().add(amount));
        account.setUpdatedAt(LocalDateTime.now());
        Account updatedAccount = accountRepository.save(account);
        
        return toDTO(updatedAccount);
    }

    public AccountDTO withdraw(String accountNumber, BigDecimal amount) {
        log.info("Withdrawing {} from account {}", amount, accountNumber);
        
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Withdrawal amount must be positive");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(amount));
        account.setUpdatedAt(LocalDateTime.now());
        Account updatedAccount = accountRepository.save(account);
        
        return toDTO(updatedAccount);
    }

    public BigDecimal getBalance(String accountNumber) {
        log.info("Getting balance for account: {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountNumber));
        return account.getBalance();
    }

    private AccountDTO toDTO(Account account) {
        return AccountDTO.builder()
                .accountNumber(account.getAccountNumber())
                .accountHolder(account.getAccountHolder())
                .balance(account.getBalance())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
