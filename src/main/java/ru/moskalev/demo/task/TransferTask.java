package ru.moskalev.demo.task;

import ru.moskalev.demo.service.BankAccountService;

import static ru.moskalev.demo.utils.TaskSimulateWork.simulateCpuWork;

public class TransferTask implements Runnable {
    private final String fromAccountNumber;
    private final String toAccountNumber;
    private final double amount;
    private final BankAccountService bankAccountService;

    public TransferTask(String fromAccountNumber, String toAccountNumber, double amount, BankAccountService bankAccountService) {
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.amount = amount;
        this.bankAccountService = bankAccountService;
    }

    @Override
    public void run() {
        try {
            System.out.println(" Поток " + Thread.currentThread().getName() +
                    " стартовал, приоритет " + Thread.currentThread().getPriority());

            simulateCpuWork("Easy task  ",2000);
            bankAccountService.transfer(fromAccountNumber, toAccountNumber, amount);
        } catch (Exception e) {
            System.err.println("Ошибка в потоке- " + Thread.currentThread().getName() +
                    ": " + e.getMessage());
            throw new RuntimeException(e);
        }


    }
}
