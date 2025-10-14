package ru.moskalev.demo.repository;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static ru.moskalev.demo.Constants.*;
import static ru.moskalev.demo.Constants.ACCOUNT_ERROR_PREFIX;

@Component
@Slf4j
public class EmailRepository {
    private Map<String, String> emails;

    @PostConstruct
    void initEmails() {
        emails = new HashMap<>();
        Random random = new Random();

        for (int i = 1; i <= 8; i++) {
            String key = String.format("ACC%03d", i);
            emails.put(key, generateEmail(key, random));
        }

        for (int i = 9; i <= ACCOUNT_COUNT; i++) {
            String key = ACCOUNT_GENERATE_PREFIX + i;
            emails.put(key, generateEmail(key, random));
        }

        for (int i = 0; i <= ACCOUNT_COUNT_WITH_PROBLEM; i++) {
            String key = ACCOUNT_ERROR_PREFIX + i;
            emails.put(key, generateEmail(key, random));
        }
    }

    public String findEmail(String accNum) {
        String email = emails.get(accNum);
        if (email == null) {
            throw new IllegalArgumentException(String.format("Email для аккаунта %s не найден", accNum));
        }
        try {
            long delay = 1L + (long) (Math.random() * 2000);//0 to 2
            log.info("EmailRepository accnum = {} , delay = {}",accNum,delay);
            Thread.sleep(delay);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info("запрос на излечение email= {} accNum={}", email, accNum);
        return email;
    }


    private String generateEmail(String key, Random random) {
        String[] domains = {"yandex.ru", "mail.ru", "example.ru"};
        String userName = key + "_user" + (1000 + random.nextInt(9000));
        String domain = domains[random.nextInt(domains.length)];
        return userName + "@" + domain;
    }
}
