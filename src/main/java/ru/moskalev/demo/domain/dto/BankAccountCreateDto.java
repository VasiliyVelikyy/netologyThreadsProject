package ru.moskalev.demo.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankAccountCreateDto {
    private String accountNumber;
    private double balance;

    private String currency;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime lastTransactionAt;

    private Integer riskScore;
    private LocalDateTime lastCredit;

}
