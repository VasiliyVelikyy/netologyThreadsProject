package ru.moskalev.demo.domain.debit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DepositRequest {
    private String accountNumber;
    private double amount;
}
