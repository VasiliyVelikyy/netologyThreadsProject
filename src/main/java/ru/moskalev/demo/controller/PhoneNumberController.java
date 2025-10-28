package ru.moskalev.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.moskalev.demo.service.PhoneNumberService;

import java.util.concurrent.atomic.AtomicInteger;

import static ru.moskalev.demo.Constants.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class PhoneNumberController {
    public final PhoneNumberService phoneNumberService;

    AtomicInteger count = new AtomicInteger(0);

    @GetMapping(URL_PHONE_BY_GOOD_ACCOUNT)
    public ResponseEntity<String> getPhoneNumber(@PathVariable String accountNumber) throws InterruptedException {
        String phone = phoneNumberService.findPhoneByAccNum(accountNumber);
        if (phone != null) {
            //long delay = 1L + (long) (Math.random() * 1000);//0 to 999
            long delay = 200;
            log.info("PhoneNumberController phone={}, delay ={}, count={} , accNumber={}",
                    phone, delay, count.incrementAndGet(), accountNumber);
            Thread.sleep(delay);

            return ResponseEntity.ok(phone);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping(URL_PHONE_BY_BAD_ACCOUNT)
    public ResponseEntity<String> getPhoneNumberWithProblem(@PathVariable String accountNumber) throws InterruptedException {
        if (accountNumber.contains(ACCOUNT_ERROR_PREFIX)) {
            String message = "Имитация сбоя при получении информации телефона для счета " + accountNumber;
            log.error(message);
            throw new RuntimeException(message);
        }

        String phone = phoneNumberService.findPhoneByAccNum(accountNumber);
        if (phone != null) {
            long delay = 1L + (long) (Math.random() * 1000);//0 to 999
            log.info("PhoneNumberController accNum={}, delay={}", accountNumber, delay);
            Thread.sleep(delay);

            return ResponseEntity.ok(phone);
        }

        return ResponseEntity.notFound().build();
    }
}
