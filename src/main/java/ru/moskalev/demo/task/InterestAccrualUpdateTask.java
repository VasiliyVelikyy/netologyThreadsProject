package ru.moskalev.demo.task;

import lombok.extern.slf4j.Slf4j;
import ru.moskalev.demo.domain.AccountUpdater;
import ru.moskalev.demo.domain.BankAccount;
import ru.moskalev.demo.service.BankAccountService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

import static ru.moskalev.demo.Constants.BATCH_FOR_FORK_JOIN;

@Slf4j
public class InterestAccrualUpdateTask extends RecursiveTask<List<AccountUpdater>> {
    private final List<BankAccount> accounts;
    private final int start;
    private final int end;

    public InterestAccrualUpdateTask(List<BankAccount> accounts) {
        this(accounts, 0, accounts.size());
    }

    public InterestAccrualUpdateTask(List<BankAccount> accounts, int start, int end) {
        this.accounts = accounts;
        this.start = start;
        this.end = end;
    }

    @Override
    protected List<AccountUpdater> compute() {
        int size = end - start;
        if (size <= BATCH_FOR_FORK_JOIN) {
            return accounts.subList(start, end).stream()
                    .map(acc -> new AccountUpdater(acc.getAccountNumber(), Math.random(acc.getBalance() * 1.1 * 100) / 100))
                    .peek(accountUpdater -> log.info("acc ={}, balance with interest ={}", accountUpdater.getAccountNumber(),
                            accountUpdater.getBalance()))
                    .collect(Collectors.toList());
        } else {
            int mid = (start + end) / 2;
            InterestAccrualUpdateTask left = new InterestAccrualUpdateTask(accounts, start, mid);
            InterestAccrualUpdateTask right = new InterestAccrualUpdateTask(accounts, mid, end);
            invokeAll(left, right);
            var result = new ArrayList<AccountUpdater>();
            result.addAll(left.join());
            result.addAll(right.join());
            return result;
        }
    }
}
