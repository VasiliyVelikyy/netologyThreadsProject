package ru.moskalev.demo.service.notification;

public interface NotificationService {
    String balanceMessage = "[NOTIFICATION] Баланс изменился";

    void onBalanceChanged(long amount, String phoneNumber);

    long getLastKnowBalance();
}
