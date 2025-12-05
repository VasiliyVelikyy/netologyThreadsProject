package ru.moskalev.demo.service.threadservice;

import org.springframework.stereotype.Service;
import ru.moskalev.demo.service.account.BankAccountServiceLock;
import ru.moskalev.demo.task.TransferTask;
import ru.moskalev.demo.utils.ErrorHandler;

@Service
public class TransferRunnableStateService {
    private final BankAccountServiceLock bankAccountServiceLock;

    public TransferRunnableStateService(BankAccountServiceLock bankAccountServiceLock) {
        this.bankAccountServiceLock = bankAccountServiceLock;
    }

    public String processRunnable() {

        Thread threadRunnable1 = new Thread(
                new TransferTask("ACC001", "ACC002", 100.0,
                        bankAccountServiceLock),
                "Transfer-Standard"
        );

        Thread threadRunnable2 = new Thread(
                new TransferTask("ACC003", "ACC004", 10.0,
                        bankAccountServiceLock),
                "Transfer-Urgent"
        );


        threadRunnable1.setPriority(Thread.NORM_PRIORITY);  // 5
        threadRunnable2.setPriority(Thread.MIN_PRIORITY);   // 10

        threadRunnable1.setUncaughtExceptionHandler(new ErrorHandler());

        threadRunnable1.start();
        threadRunnable2.start();
        return "ok!";
    }
}
