package ru.moskalev.demo.service.exproblem;

import org.springframework.stereotype.Service;
import ru.moskalev.demo.service.account.BankAccountServiceLock;
import ru.moskalev.demo.service.notification.SmsNotificatorService;

@Service
public class ProcessReentrantLockService {
    private final CashWithdrawalService cashWithdrawalService;
    private final SmsNotificatorService smsNotificatorService;
    private final WithdrawalVerificationService withdrawalVerificationService;
    private final BankAccountServiceLock bankAccountServiceLock;

    public ProcessReentrantLockService(CashWithdrawalService cashWithdrawalService, SmsNotificatorService smsNotificatorService, WithdrawalVerificationService withdrawalVerificationService, BankAccountServiceLock bankAccountServiceLock) {
        this.cashWithdrawalService = cashWithdrawalService;
        this.smsNotificatorService = smsNotificatorService;
        this.withdrawalVerificationService = withdrawalVerificationService;
        this.bankAccountServiceLock = bankAccountServiceLock;
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

    public String processWithdrawalDeposit() throws InterruptedException {
        Thread writer1 = new Thread(() -> bankAccountServiceLock.deposit("ACC001", 500), "Popolnenie-1");
        Thread writer2 = new Thread(() -> bankAccountServiceLock.withdraw("ACC001", 200), "Popolnenie-2");
        Thread writer3 = new Thread(() -> bankAccountServiceLock.deposit("ACC001", 300), "Popolnenie-2");

        Thread[] readers = new Thread[5];

        for (int i = 0; i < 5; i++) {
            readers[i] = new Thread(() -> bankAccountServiceLock.printBalance("ACC001"), "Поток чтение-" + i);
            readers[i].start();
        }

        writer1.start();
        Thread.sleep(600);
        writer2.start();
        writer3.start();

        writer1.join();
        writer2.join();
        writer3.join();

        for (Thread reader : readers) {
            reader.join();
        }

        System.out.println("operation is done");
        return "ok";
    }

    public String processDowngrade() throws InterruptedException {

        Thread writerWithDowngrade = new Thread(() -> {
            bankAccountServiceLock.depositWithDowngrade("ACC001", 500);
        }, "Пополнение+Downgrade");


        Thread writerNormal = new Thread(() -> {
            bankAccountServiceLock.withdraw("ACC001", 300);
        }, "Списание-обычное");


        Thread writerDep = new Thread(() -> {
            bankAccountServiceLock.deposit("ACC001", 300);
        }, "Списание-обычное");

        //  Читатели — должны запуститься параллельно во время read-фазы downgrade
        Thread[] readers = new Thread[3];
        for (int i = 0; i < 3; i++) {
            final int id = i + 1;
            readers[i] = new Thread(() -> {
                bankAccountServiceLock.printBalance("ACC001");
            }, "Чтение-" + id);
        }

        // Запускаем всё почти одновременно:
        writerWithDowngrade.start();
        Thread.sleep(10); // даём ему немного форы, чтобы он начал downgrade

        for (Thread reader : readers) {
            reader.start();
        }

        Thread.sleep(1000); // ждём, пока читатели начнутся

        writerNormal.start(); // этот должен заблокироваться до конца read-фазы downgrade
        writerDep.start();

        // Ждём завершения всех
        writerWithDowngrade.join();
        for (Thread reader : readers) {
            reader.join();
        }
        writerNormal.join();

        System.out.println("=".repeat(80));
        return "ok";

    }
}
