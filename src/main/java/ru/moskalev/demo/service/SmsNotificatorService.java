package ru.moskalev.demo.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class SmsNotificatorService {
    private final ReentrantLock smsLock = new ReentrantLock();

    public void trySendSms(String phone, String message) {
        boolean lockAcquired;

        try {
            lockAcquired = smsLock.tryLock(500, TimeUnit.MILLISECONDS);
            if (lockAcquired) {
                try {
                    System.out.println("SMS ОТПРАВЛЕН на номер " + phone + " message " + message);
                    Thread.sleep(1000);
                    System.out.println("Успех");
                } finally {
                    smsLock.unlock();
                }
            } else {
                System.out.println("Sms Пропущен не удалось отправить " + message + " на номер" + phone + " Клиент получает другое смс");
                //todo сложить в отложенные задачи
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
