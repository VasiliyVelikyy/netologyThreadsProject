package ru.moskalev.demo.service.notification;

import org.springframework.stereotype.Service;

@Service
public class BalanceNotificationWithVolatileService implements NotificationService {
    private final SmsNotificatorService smsNotificatorService;

    private volatile long lastBalanceKnow;

    public BalanceNotificationWithVolatileService(SmsNotificatorService smsNotificatorService) {
        this.smsNotificatorService = smsNotificatorService;
    }

    @Override
    public void onBalanceChanged(long newBalance, String phoneNumber) {
        if (newBalance != lastBalanceKnow) {
            smsNotificatorService.trySendSms(newBalance, lastBalanceKnow);
            lastBalanceKnow = newBalance;
        }

    }

    @Override
    public long getLastKnowBalance() {
        return lastBalanceKnow;
    }
}
