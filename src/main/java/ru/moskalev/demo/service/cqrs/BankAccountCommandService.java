package ru.moskalev.demo.service.cqrs;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.moskalev.demo.domain.account.AccountUpdateEvent;
import ru.moskalev.demo.repository.BankAccountRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankAccountCommandService {
    private final BankAccountRepository bankAccountRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${account.deposit.topic}")
    private String accountDepositTopic;

    @Transactional
    public void deposit(String accountNumber, double amount) {
        try {
            var acc = bankAccountRepository.findById(accountNumber).orElseThrow(() -> new RuntimeException("Account not found " + accountNumber));
            acc.setBalance(acc.getBalance() + amount);
            bankAccountRepository.save(acc);

            String eventJson = objectMapper.writeValueAsString(new AccountUpdateEvent(accountNumber, acc.getBalance(), Instant.now()));
            kafkaTemplate.send(accountDepositTopic, accountNumber, eventJson);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

    }
}
