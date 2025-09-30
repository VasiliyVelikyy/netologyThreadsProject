package ru.moskalev.demo.service.exproblem;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import ru.moskalev.demo.service.BankAccountService;
import ru.moskalev.demo.data.TransferGeneratorService;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

import static ru.moskalev.demo.Constants.ACCOUNT_COUNT;
import static ru.moskalev.demo.utils.TimeUtils.evaluateExecutionTime;

@Service
public class StreamTransferService implements ApplicationRunner {
    private final TransferGeneratorService transferGeneratorService;
    private final BankAccountService bankAccountService;

    private List<TransferGeneratorService.TransferOperation> operations;

    public StreamTransferService(TransferGeneratorService transferGeneratorService,
                                 BankAccountService bankAccountService) {
        this.transferGeneratorService = transferGeneratorService;
        this.bankAccountService = bankAccountService;
    }

    @Override
    public void run(ApplicationArguments args) {
        operations = transferGeneratorService.generateTransfers(ACCOUNT_COUNT);
    }

    public String startStream() {
        long start = System.nanoTime();

        operations.stream()
                .forEach(op -> bankAccountService.transferWithDoubleSync(op.from(), op.to(), op.amount()));

        return evaluateExecutionTime(start);
    }

    public String startParallelStream() {
        long start = System.nanoTime();
        operations.parallelStream()
                .forEach(op -> bankAccountService.transferWithDoubleSync(op.from(), op.to(), op.amount()));

        return evaluateExecutionTime(start);
    }


    public String startParallelStreamBlock() {
        long start = System.nanoTime();

        operations.parallelStream()
                .forEach(op -> bankAccountService.transferBlockOneMonitor(op.from(), op.to(), op.amount()));

        return evaluateExecutionTime(start);
    }

    public String startForkJoinPoolParallelStream() {
        long start = System.nanoTime();

        ForkJoinPool customPool = new ForkJoinPool(4); // 4 потока

        customPool.submit(() ->
                operations.parallelStream()
                        .forEach(op -> bankAccountService.transferWithDoubleSync(op.from(), op.to(), op.amount()))
        ).join();

        customPool.shutdown();

        return evaluateExecutionTime(start);
    }
}