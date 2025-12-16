package ru.moskalev.demo.domain.dto;

public record BankAccountDto(String accountNumber,
                             double balance,
                             String currency,
                             String status,
                             Integer riskScore) {
}
