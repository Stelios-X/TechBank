package com.techbank.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private Long transactionId;
    private String sourceAccount;
    private String destinationAccount;
    private BigDecimal amount;
    private String transactionType;
    private String status;
    private String description;
    private LocalDateTime createdAt;
}
