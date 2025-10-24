package ru.moskalev.demo.domain.clientinfo;

public record ClientFullInfo(
        String account,
        double balance,
        String phoneNumber
) {
}
