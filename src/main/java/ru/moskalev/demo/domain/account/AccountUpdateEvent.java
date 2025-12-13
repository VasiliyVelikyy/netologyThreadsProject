package ru.moskalev.demo.domain.account;

import java.time.Instant;

public record AccountUpdateEvent(String accNumber,
                                 double balance,
                                 Instant timestamp) {
}
