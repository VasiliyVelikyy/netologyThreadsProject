package ru.moskalev.demo.service.notification;

import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
public class BalanceNotificationWithAtomicService implements NotificationService {
    private final SmsNotificatorService smsNotificatorService;

    public BalanceNotificationWithAtomicService(SmsNotificatorService smsNotificatorService) {
        this.smsNotificatorService = smsNotificatorService;
    }

    private AtomicLong atomicBalance = new AtomicLong(0);

    @Override
    public void onBalanceChanged(long amount, String phoneNumber) {
        long newBalance = atomicBalance.addAndGet(amount);
        System.out.println(Thread.currentThread().getName() + " Установил баланс " + newBalance);
        sendNotify(amount);


    }

    synchronized void sendNotify(long amount) {
        smsNotificatorService.trySendSms(amount);
    }

    @Override
    public long getLastKnowBalance() {
        return atomicBalance.get();
    }
}
