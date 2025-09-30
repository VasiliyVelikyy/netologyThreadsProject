package ru.moskalev.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static ru.moskalev.demo.Constants.ACCOUNT_COUNT;
import static ru.moskalev.demo.utils.TimeUtils.evaluateExecutionTime;

@Service
@Slf4j
public class PhoneNumberService {
    private final Map<String, String> phoneDb;

    public PhoneNumberService() {
        long startTime = System.nanoTime();
        this.phoneDb = generateData();
        log.info("Сгенерировано {} номеров", phoneDb.size());

        evaluateExecutionTime(startTime);
    }


    public String findPhoneByAccNum(String accountNumber) throws InterruptedException {
        Thread.sleep(10);
        var elem = phoneDb.get(accountNumber);

        if(elem== null){
            throw new RuntimeException("телефон не найден по аккаунту "+accountNumber);
        }
        return elem;

    }

    private Map<String, String> generateData() {
        Map<String, String> map = new HashMap<>(ACCOUNT_COUNT);
        for (int i = 1; i <= 8; i++) {
            String key = String.format("ACC%03d", i);
            map.put(key, generateRandomPhone());
        }

        for (int i = 9; i <= ACCOUNT_COUNT; i++) {
            String key = "GEN_ACC_-" + i;
            map.put(key, generateRandomPhone());
        }

        return Collections.unmodifiableMap(map);
    }

    private String generateRandomPhone() {
        int lenght = ThreadLocalRandom.current().nextInt(10, 13);
        StringBuilder sb = new StringBuilder("+");
        for (int i = 0; i < lenght; i++) {
            sb.append(ThreadLocalRandom.current().nextInt(10));
        }
        return sb.toString();
    }

}
