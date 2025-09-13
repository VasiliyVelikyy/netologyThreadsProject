package ru.moskalev.demo.task;

import ru.moskalev.demo.repository.BankAccountRepository;

import java.time.LocalDate;

public class LoggerTask implements Runnable {

    private BankAccountRepository bankAccountRepository;
    private boolean running = true;

    public LoggerTask(BankAccountRepository bankAccountRepository) {
        this.bankAccountRepository = bankAccountRepository;
    }

    @Override
    public void run() {
        while (running) {
            try {
                long count = bankAccountRepository.count();
                System.out.printf(LocalDate.now() + " : INFO [Демон поток nameThread " + Thread.currentThread().getName()
                        + "] Количества счетов %d%n", count);
                Thread.sleep(30000);
            } catch (InterruptedException e) {

                System.out.println("Демон логгер остановлен " + Thread.currentThread().getName());
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Демон, ошибка " + e.getMessage());
                break;
            }
        }

    }
}
