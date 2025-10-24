package ru.moskalev.demo.domain.credit;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreditApplicationDto {
    private String accountNumber;
    private double amount;
}
