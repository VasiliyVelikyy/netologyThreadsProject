package ru.moskalev.demo.task;

import ru.moskalev.demo.domain.BankAccount;

import java.util.List;
import java.util.concurrent.RecursiveTask;

import static ru.moskalev.demo.Constants.BATCH_FOR_FORK_JOIN;

public class BalanceSumTask extends RecursiveTask<Long> {
    private final List<BankAccount> accounts;
    private final int start;
    private final int end;

    public BalanceSumTask(List<BankAccount> accounts) {
        this(accounts, 0, accounts.size());
    }

    public BalanceSumTask(List<BankAccount> accounts, int start, int end) {
        this.accounts = accounts;
        this.start = start;
        this.end = end;
    }

//    @Override
//    protected Long compute() {
//        int size = end - start;
//        if (size <= BATCH_FOR_FORK_JOIN) {
//            long sum = 0;
//            for (int i = start; i < end; i++) {
//                var currAcc = accounts.get(i);
//                sum += currAcc.getBalance();
//
//            }
//            return sum;
//        } else {
//            int mid = (start + end) / 2;
//            BalanceSumTask left = new BalanceSumTask(accounts, start, mid);
//            BalanceSumTask right = new BalanceSumTask(accounts, mid, end);
//            invokeAll(left, right);
//            return left.join() + right.join();
//        }
//    }

    @Override
    protected Long compute() {
        int size = end - start;
        if (size <= BATCH_FOR_FORK_JOIN) {
            long sum = 0;
            for (int i = start; i < end; i++) {
                var currAcc = accounts.get(i);
                sum += currAcc.getBalance();

            }
            return sum;
        } else {
            int mid = (start + end) >>> 1;
            BalanceSumTask left = new BalanceSumTask(accounts, start, mid);
            BalanceSumTask right = new BalanceSumTask(accounts, mid, end);

            left.fork();
            long rightResult = right.compute();

            long leftResult = left.join();
            return leftResult + rightResult;
        }
    }
}
