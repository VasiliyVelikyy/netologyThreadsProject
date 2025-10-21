package ru.moskalev.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.moskalev.demo.task.BalanceSumTask;

import java.util.concurrent.ForkJoinPool;

import static ru.moskalev.demo.utils.TimeUtils.evaluateExecutionTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class BankBalanceSummatorService {
    private final BankAccountService bankAccountService;

    private final ForkJoinPool allSumPool = new ForkJoinPool(10);

    public Long getSumAllAccRecursive() {
        long startTime = System.nanoTime();
        var accounts = bankAccountService.getAllAcc();
        BalanceSumTask task = new BalanceSumTask(accounts);
        long totalBalance = allSumPool.invoke(task);

        logResult(totalBalance);

        evaluateExecutionTime(startTime);
        return totalBalance;

    }

    private static void logResult(long totalBalance) {
        log.info("Общий баланс по всем счетам ={}", totalBalance);
    }

    public Long getSumAllAccFullIteration() {
        long startTime = System.nanoTime();
        var account = bankAccountService.getAllAcc();

        long totalBalance = 0;
        for (int i = 0; i < account.size(); i++) {
            totalBalance += account.get(i).getBalance();
        }
        logResult(totalBalance);

        evaluateExecutionTime(startTime);
        return totalBalance;
    }
}
