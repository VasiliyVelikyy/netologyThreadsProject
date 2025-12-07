package ru.moskalev.demo.service.exproblem;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.moskalev.demo.repository.BankAccountRepository;
import ru.moskalev.demo.service.account.BankAccountService;
import ru.moskalev.demo.service.account.BankAccountServiceLock;


@Service
@RequiredArgsConstructor
public class TransferProblemService {
    private final BankAccountServiceLock bankAccountServiceLock;
    private final BankAccountRepository bankAccountRepository;
    private final BankAccountService bankAccountService;

    private static volatile boolean isAvailableTransfer = true;
    private static final Object transferMonitor = new Object();
    private static final int MAX_ATTEMPTS = 10;




    public String transferRaceCondition() {
        Thread t1 = new Thread(() -> {
            try {
                bankAccountServiceLock.transfer("ACC001", "ACC002", 50);
            } catch (Exception e) {
                System.out.println("Ошибка в потоке" + Thread.currentThread().getName());
            }

        }, "transactionOne");

        Thread t2 = new Thread(() -> {
            try {
                bankAccountServiceLock.transfer("ACC001", "ACC002", 50);
            } catch (Exception e) {
                System.out.println("Ошибка в потоке" + Thread.currentThread().getName());
            }

        }, "transactionTwo");

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (Exception e) {
            System.out.println("Ошибка в потоке" + Thread.currentThread().getName());
        }

        double finalBalance = bankAccountService.getAccount("ACC001").getBalance();
        String message = "Итоговый баланс ACC001  сумма=" + finalBalance;
        System.out.println(message);

        return message;
    }

    public String transferRaceConditionSync() {
        Thread t1 = new Thread(() -> {
            try {
                bankAccountServiceLock.transferBlockOneMonitor("ACC001", "ACC002", 50);
            } catch (Exception e) {
                System.out.println("Ошибка в потоке" + Thread.currentThread().getName());
            }

        }, "transactionOne");

        Thread t2 = new Thread(() -> {
            try {
                bankAccountServiceLock.transferBlockOneMonitor("ACC001", "ACC002", 50);
            } catch (Exception e) {
                System.out.println("Ошибка в потоке" + Thread.currentThread().getName());
            }

        }, "transactionTwo");

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (Exception e) {
            System.out.println("Ошибка в потоке" + Thread.currentThread().getName());
        }

        double finalBalance = bankAccountService.getAccount("ACC001").getBalance();
        String message = "Итоговый баланс ACC001  сумма=" + finalBalance;
        System.out.println(message);

        return message;
    }

    public String transferDeadlock() {

        Thread t1 = new Thread(() -> {
            try {
                Thread.sleep(100);
                //bankAccountService.transferDeadLock("ACC001", "ACC002", 50);
                bankAccountServiceLock.transferWithDoubleSync("ACC001", "ACC002", 50);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                Thread.sleep(100);
                // bankAccountService.transferDeadLock("ACC002", "ACC001", 50);
                bankAccountServiceLock.transferWithDoubleSync("ACC001", "ACC002", 50);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        });

        t1.start();
        t2.start();

        try {
            Thread.sleep(3000); // Ждём 3 секунды
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (t1.isAlive() && t2.isAlive()) {
            String message = "DEADLOCK обнаружен — оба потока зависли!";
            System.out.println(message);
            return message;
        } else {
            String message = "Deadlock не произошёл — потоки завершились.";
            System.out.println(message);
            return message;
        }
    }

    public String transferLivelock() throws InterruptedException {

        Thread t1 = new Thread(() -> {
            try {
                while (isAvailableTransfer) {
                    System.out.println("Клиент-1  уступаю Клиенту2");
                    try {
                        Thread.sleep(100);

                    } catch (Exception e) {

                    }

                    if (isAvailableTransfer) {
                        System.out.println("Клиент1: Клиент 2 не выполнил перевод уступаю снова");
                    } else {
                        System.out.println("Клиент1: Клиент 2  выполнил перевод. Могу сделать перевод");
                        bankAccountServiceLock.transfer("ACC001", "ACC002", 50);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                while (isAvailableTransfer) {
                    System.out.println("Клиент-2  уступаю Клиенту1");
                    try {
                        Thread.sleep(100);

                    } catch (Exception e) {

                    }

                    if (isAvailableTransfer) {
                        System.out.println("Клиент2: Клиент 1 не выполнил перевод уступаю снова");
                    } else {
                        System.out.println("Клиент2: Клиент 1  выполнил перевод. Могу сделать перевод");
                        bankAccountServiceLock.transfer("ACC001", "ACC002", 50);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        t1.start();
        t2.start();


        t1.join();
        t2.join();


        return "Livelock продемонстрирован";
    }

    public String transferLivelockWithMaxAttempt() throws InterruptedException {

        Thread client1 = new Thread(() -> {
            int attempts = 0;
            while (isAvailableTransfer && attempts < MAX_ATTEMPTS) {
                //  client1 имеет приоритет — после нескольких уступок он действует
                if (attempts < 3) {
                    System.out.println("client1: Я вежливо уступаю...");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        break;
                    }
                } else {
                    System.out.println("client1: Я приоритет, совершаю перевод!");
                    synchronized (transferMonitor) {
                        if (isAvailableTransfer) {
                            bankAccountServiceLock.transfer("ACC002", "ACC001", 50);

                            isAvailableTransfer = false;
                            System.out.println("client1: совершил перевод! ");
                            return;
                        }
                    }
                }
                attempts++;
            }


        }, "Client1");

        Thread client2 = new Thread(() -> {
            while (isAvailableTransfer) {
                // Client2 всегда уступает Client1
                System.out.println("Client2: Я уступаю Client1...");
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    break;
                }

                // Проверяем, не выполнила ли перевод Client1
                if (!isAvailableTransfer) {
                    synchronized (transferMonitor) {
                        System.out.println("Client2: Теперь моя очередь делать перевод ");
                        bankAccountServiceLock.transfer("ACC002", "ACC001", 50);

                    }
                    return;
                }
            }

        }, "Client2");

        // Запускаем потоки
        client1.start();
        client2.start();

        // Ждём завершения
        client1.join();
        client2.join();

        return "Трансфер завершен.";
    }

    public String transferStarvation() throws InterruptedException {

        Object accountLock = new Object();

        Thread advertistingBanner = new Thread(() -> {
            System.out.println("advertistingBanner strart");
            while (true) {
                synchronized (accountLock) {
                    System.out.println("Пытаюсь отправить рекламный баннер клиенту");
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }, "Client-advertistingBanner");

        Thread transferClient = new Thread(() -> {
            System.out.println("Client2 запущен");
            while (true) {
                synchronized (accountLock) {
                    System.out.println("Client2 пытаюсь сделать перевод");
                    bankAccountServiceLock.transfer("ACC001", "ACC002", 1);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }, "Client-transferClient");

        advertistingBanner.start();
        transferClient.start();

        advertistingBanner.join();
        transferClient.join();

        Thread.sleep(3000);
        return "ok";
    }
}
