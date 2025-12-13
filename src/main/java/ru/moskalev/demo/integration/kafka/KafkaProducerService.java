package ru.moskalev.demo.integration.kafka;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${account.created.topic}")
    private String accountCreatedTopic;

    public void sendAccountCreated(String accountNumber) {
        kafkaTemplate.send(accountCreatedTopic, accountNumber);
        log.info("Send to kafka " + accountNumber);
    }


}
