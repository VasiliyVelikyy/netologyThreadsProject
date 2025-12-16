package ru.moskalev.demo.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "account_info")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccountInfo {

    @Id
    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_transaction_at")
    private LocalDateTime lastTransactionAt;

    @OneToOne
    @MapsId
    @JoinColumn(name = "account_number", referencedColumnName = "account_number")
    private BankAccount bankAccount;
}
