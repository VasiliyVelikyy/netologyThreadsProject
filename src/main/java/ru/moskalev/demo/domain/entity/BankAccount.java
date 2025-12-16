package ru.moskalev.demo.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "bank_accounts")
@Getter
@Setter
public class BankAccount {

    @Id
    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "balance", nullable = false)
    private double balance;

    @OneToOne(mappedBy = "bankAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private AccountInfo accountInfo;

    @OneToOne(mappedBy = "bankAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private AccountRisk accountRisk;

    public BankAccount() {
    }

    public BankAccount(String accountNumber, double balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "BankAccount{" +
                "accountNumber='" + accountNumber + '\'' +
                ", balance=" + balance +
                '}';
    }
}
