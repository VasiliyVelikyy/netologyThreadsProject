package ru.moskalev.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.moskalev.demo.service.PhoneNumberService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PhoneNumberController {
    public final PhoneNumberService phoneNumberService;

    @GetMapping("/account/{accountNumber}/phone")
    public ResponseEntity<String> getPhoneNumber(@PathVariable String accountNumber) throws InterruptedException {
        String phone = phoneNumberService.findPhoneByAccNum(accountNumber);
        if (phone != null) {
            return ResponseEntity.ok(phone);
        }
        return ResponseEntity.notFound().build();
    }
}
