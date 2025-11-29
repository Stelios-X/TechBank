package com.techbank.account.controller;

import com.techbank.account.service.AccountService;
import com.techbank.common.dto.AccountDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/create")
    public ResponseEntity<AccountDTO> createAccount(
            @RequestParam String accountNumber,
            @RequestParam String accountHolder) {
        AccountDTO account = accountService.createAccount(accountNumber, accountHolder);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountDTO> getAccount(@PathVariable String accountNumber) {
        AccountDTO account = accountService.getAccount(accountNumber);
        return ResponseEntity.ok(account);
    }

    @GetMapping
    public ResponseEntity<List<AccountDTO>> getAllAccounts() {
        List<AccountDTO> accounts = accountService.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<AccountDTO> deposit(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount) {
        AccountDTO account = accountService.deposit(accountNumber, amount);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<AccountDTO> withdraw(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount) {
        AccountDTO account = accountService.withdraw(accountNumber, amount);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable String accountNumber) {
        BigDecimal balance = accountService.getBalance(accountNumber);
        return ResponseEntity.ok(balance);
    }
}
