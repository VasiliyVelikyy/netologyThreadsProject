package ru.moskalev.demo.service.exproblem;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import ru.moskalev.demo.service.account.BankAccountServiceLock;
import ru.moskalev.demo.generators.TransferGeneratorService;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

import static ru.moskalev.demo.Constants.ACCOUNT_COUNT;
import static ru.moskalev.demo.utils.TimeUtils.evaluateExecutionTime;

@Service
public class StreamTransferService implements ApplicationRunner {
    private final TransferGeneratorService transferGeneratorService;
    private final BankAccountServiceLock bankAccountServiceLock;

    private List<TransferGeneratorService.TransferOperation> operations;

    public StreamTransferService(TransferGeneratorService transferGeneratorService,
                                 BankAccountServiceLock bankAccountServiceLock) {
        this.transferGeneratorService = transferGeneratorService;
        this.bankAccountServiceLock = bankAccountServiceLock;
    }

    @Override
    public void run(ApplicationArguments args) {
        operations = transferGeneratorService.generateHotAccTransfers(ACCOUNT_COUNT);
    }

    public String startStream() {
        long start = System.nanoTime();

        operations.stream()
                .forEach(op -> bankAccountServiceLock.transferWithDoubleSync(op.from(), op.to(), op.amount()));

        return evaluateExecutionTime(start);
    }

    public String startParallelStream() {
        long start = System.nanoTime();
        operations.parallelStream()
                .forEach(op -> bankAccountServiceLock.transferWithDoubleSync(op.from(), op.to(), op.amount()));

        return evaluateExecutionTime(start);
    }


    public String startParallelStreamBlock() {
        long start = System.nanoTime();

        operations.parallelStream()
                .forEach(op -> bankAccountServiceLock.transferBlockOneMonitor(op.from(), op.to(), op.amount()));

        return evaluateExecutionTime(start);
    }

    public String startForkJoinPoolParallelStream() {
        long start = System.nanoTime();

        ForkJoinPool customPool = new ForkJoinPool(4); // 4 потока

        customPool.submit(() ->
                operations.parallelStream()
                        .forEach(op -> bankAccountServiceLock.transferWithDoubleSync(op.from(), op.to(), op.amount()))
        ).join();

        customPool.shutdown();

        return evaluateExecutionTime(start);
    }

    public List<TransferGeneratorService.TransferOperation> getOperations() {
        return operations;
    }
}