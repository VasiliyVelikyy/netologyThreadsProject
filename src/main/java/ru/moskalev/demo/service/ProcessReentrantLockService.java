package ru.moskalev.demo.service;

import org.springframework.stereotype.Service;

@Service
public class ProcessReentrantLockService {
    private final CashWithdrawalService cashWithdrawalService;
    private final SmsNotificatorService smsNotificatorService;
    private final WithdrawalVerificationService withdrawalVerificationService;

    public ProcessReentrantLockService(CashWithdrawalService cashWithdrawalService, SmsNotificatorService smsNotificatorService, WithdrawalVerificationService withdrawalVerificationService) {
        this.cashWithdrawalService = cashWithdrawalService;
        this.smsNotificatorService = smsNotificatorService;
        this.withdrawalVerificationService = withdrawalVerificationService;
    }

    public String processSemaphoreWithdrawal() throws InterruptedException {
        Runnable clientTask = () -> {
            String name = Thread.currentThread().getName();
            cashWithdrawalService.withdrawalCash(name, 10_000);
        };

        Thread c1 = new Thread(clientTask, "Client-1");
        Thread c2 = new Thread(clientTask, "Client-2");
        Thread c3 = new Thread(clientTask, "Client-3");
        Thread c4 = new Thread(clientTask, "Client-4");
        Thread c5 = new Thread(clientTask, "Client-5");
        Thread c6 = new Thread(clientTask, "Client-6");

        c1.start();
        c2.start();
        c3.start();
        c4.start();
        c5.start();
        c6.start();

        c1.join();
        c2.join();
        c3.join();
        c4.join();
        c5.join();
        c6.join();

        var message = "Все клиенты обслужены";
        System.out.println(message);
        return message;
    }

    public String processSmsNotify() throws InterruptedException {
        String phone = "+790009009999";

        Thread t1 = new Thread(() ->
                smsNotificatorService.trySendSms(phone, "Sms1 ваш баланс"));

        Thread t2 = new Thread(() -> {
            try {
                Thread.sleep(100);
                smsNotificatorService.trySendSms(phone, "Sms2 ваш баланс");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        var message = "Тест заверешен";
        System.out.println(message);
        return message;
    }

    public String processWithdrawalLarge() throws InterruptedException {
        Thread clientGenerator = new Thread(() -> {
            try {
                for (int i = 1; i <= 8; i++) {
                    String accNum = "ACC00" + i;
                    double amount = Math.round(10000 + Math.random() * 10000); //от 10к до 20к
                    System.out.println("Генератор создла заявку " + i + " снятие " + amount + " рубли со счета" + accNum);

                    try {
                        withdrawalVerificationService.requestLargeWithdrawal(accNum, amount);
                        System.out.println("Генератор ,заявка " + i + " для аккаунта успешно обработана " + accNum);
                    } catch (InterruptedException e) {
                        System.err.println(e.getMessage());
                        return;
                    }
                }

            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }, "Client-generator");


        Thread manager1 = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    withdrawalVerificationService.waitForRequests();
                    Thread.sleep(700);
                    withdrawalVerificationService.approveNextWithdrawal();
                }
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }

        }, "Manager-1");

        Thread manager2 = new Thread(() -> {
            try {
                for (int i = 0; i < 3; i++) {
                    withdrawalVerificationService.waitForRequests();
                    Thread.sleep(900);
                    withdrawalVerificationService.approveNextWithdrawal();
                }
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }

        }, "Manager-2");


        clientGenerator.start();
        manager1.start();
        manager2.start();

        clientGenerator.join();
        manager1.join();
        manager2.join();


        var message = "Все 8 заявок успешно обработаны";
        System.out.println(message);
        return message;
    }
}
