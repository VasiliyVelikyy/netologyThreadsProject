package ru.moskalev.demo.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountUpdater {
    private String accountNumber;

    private double balance;
}
