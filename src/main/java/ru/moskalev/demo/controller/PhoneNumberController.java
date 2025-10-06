package ru.moskalev.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.moskalev.demo.service.PhoneNumberService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class PhoneNumberController {
    public final PhoneNumberService phoneNumberService;

    @GetMapping("/account/{accountNumber}/phone")
    public ResponseEntity<String> getPhoneNumber(@PathVariable String accountNumber) throws InterruptedException {
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
