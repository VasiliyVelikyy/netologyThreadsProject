package ru.moskalev.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.moskalev.demo.task.InterestAccrualAction;

import java.util.concurrent.ForkJoinPool;

@Component
@Slf4j
@RequiredArgsConstructor
public class BankInterestAccrualService {

    private final BankAccountService bankAccountService;

    private final ForkJoinPool interestPool = new ForkJoinPool(10);

    public void calculateInterest(){
        var accounts = bankAccountService.getAllAcc();
        log.info("До начисления {}",accounts.get(0));
        interestPool.invoke(new InterestAccrualAction(accounts,bankAccountService));

        log.info("После начисления начисления {}",accounts.get(0));
    }
}
