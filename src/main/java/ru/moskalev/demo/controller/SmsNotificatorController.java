package ru.moskalev.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.moskalev.demo.service.ProcessReentrantLockService;

@RestController
public class SmsNotificatorController {
    private final ProcessReentrantLockService processReentrantLockService;

    public SmsNotificatorController(ProcessReentrantLockService processReentrantLockService) {
        this.processReentrantLockService = processReentrantLockService;
    }

    @GetMapping("/process-sms-notification")
    public String processSmsNotify() throws InterruptedException {
        return processReentrantLockService.processSmsNotify();
    }
}
