package ru.moskalev.demo.domain.entity;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "account_risk")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccountRisk {

    @Id
    private String accountNumber;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "last_credit")
    private LocalDateTime lastCredit;

    @OneToOne
    @MapsId
    @JoinColumn(name = "account_number", referencedColumnName = "account_number")
    private BankAccount bankAccount;
}
