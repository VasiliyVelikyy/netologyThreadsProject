package ru.moskalev.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static ru.moskalev.demo.Constants.ACCOUNT_ERROR_PREFIX;

@Service
@Slf4j
public class VerificationEmailService {
    public CompletableFuture<Boolean> isEmailVeifiedAsync(String email) {
        log.info("запрос на верифкацию ={}", email);
        return CompletableFuture.completedFuture(!email.contains(ACCOUNT_ERROR_PREFIX));
    }
}
