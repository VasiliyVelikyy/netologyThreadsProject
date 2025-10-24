package ru.moskalev.demo.task;

import lombok.extern.slf4j.Slf4j;
import ru.moskalev.demo.domain.account.BankAccount;
import ru.moskalev.demo.service.BankAccountService;

import java.util.List;
import java.util.concurrent.RecursiveAction;

import static ru.moskalev.demo.Constants.BATCH_FOR_FORK_JOIN;

@Slf4j
public class InterestAccrualAction extends RecursiveAction {
    private BankAccountService bankAccountService;
    private final List<BankAccount> accounts;
    private final int start;
    private final int end;

    public InterestAccrualAction(List<BankAccount> accounts, BankAccountService bankAccountService) {
        this(accounts, 0, accounts.size(),bankAccountService);
    }

    public InterestAccrualAction(List<BankAccount> accounts, int start, int end, BankAccountService bankAccountService) {
        this.accounts = accounts;
        this.start = start;
        this.end = end;
        this.bankAccountService = bankAccountService;
    }

    @Override
    protected void compute() {
        int size = end - start;
        if (size <= BATCH_FOR_FORK_JOIN) {
            for (int i = start; i < end; i++) {
                var currAcc = accounts.get(i);
                double interest = currAcc.getBalance() / 10;
                double newBalance = currAcc.getBalance() + interest;

                log.info("acc ={}, oldBalance={}, balance with inerest={}", currAcc.getAccountNumber(),
                        currAcc.getBalance(), newBalance);
                currAcc.setBalance(newBalance);
                bankAccountService.save(currAcc);
            }
        } else {
            int mid = (start + end) / 2;
            InterestAccrualAction left = new InterestAccrualAction(accounts, start, mid, bankAccountService);
            InterestAccrualAction right = new InterestAccrualAction(accounts, mid, end, bankAccountService);
            invokeAll(left, right);
        }
    }
}
