package ru.moskalev.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.moskalev.demo.service.notification.AsyncNotificationService;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final AsyncNotificationService notificationService;

    @PostMapping("/check-low-balance")
    public ResponseEntity<String> tiggerLowBalance() {
        notificationService.checkAndNotifyLowBalanceAsync();
        return ResponseEntity.accepted().body("Проверка низкого баланса запущена в фоне. Следите за логами");
    }


}
