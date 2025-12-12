package ru.moskalev.demo.integration;

import java.util.concurrent.CompletableFuture;

public interface PhoneNumberPort {
    CompletableFuture<String>getPhoneNumberAsync(String accNumber);
}
