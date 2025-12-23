package ru.moskalev.demo.domain.documents;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "bank_account_profiles")
@Getter
@Setter
public class BankAccountDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String accountNumber;
    private double balance;
    private String currency;
    private String status;
    private Integer riskScore;

    public BankAccountDocument() {
    }

    public BankAccountDocument(String accountNumber, double balance, String currency, String status, Integer riskScore) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.currency = currency;
        this.status = status;
        this.riskScore = riskScore;
    }
}
