package ru.moskalev.demo.domain;

public record ClientFullInfo(
        String account,
        double balance,
        String phoneNumber
) {
}
