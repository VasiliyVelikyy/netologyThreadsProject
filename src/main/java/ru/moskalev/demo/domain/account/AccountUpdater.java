package ru.moskalev.demo.domain.account;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountUpdater {
    private String accountNumber;

    private double balance;

    public AccountUpdater(String accountNumber, double balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }
}
