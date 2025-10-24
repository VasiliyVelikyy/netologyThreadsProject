package ru.moskalev.demo.domain.credit;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreditApplication {
    private String accountNumber;
    private double amount;
    private LocalDateTime submissionTime;

    public CreditApplication(String accountNumber, double amount) {
        this.accountNumber = accountNumber;
        this.amount = amount;
        submissionTime = LocalDateTime.now();
    }
}
