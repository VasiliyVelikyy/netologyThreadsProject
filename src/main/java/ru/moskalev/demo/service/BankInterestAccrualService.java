package ru.moskalev.demo.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.moskalev.demo.domain.AccountUpdater;
import ru.moskalev.demo.task.InterestAccrualUpdateTask;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

@Component
@Slf4j
@RequiredArgsConstructor
public class BankInterestAccrualService {


    private final ForkJoinPool interestPool = new ForkJoinPool(10);

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void calculateInterest() {
        List<AccountUpdater> balances = em.createQuery(
                        "SELECT new ru.moskalev.demo.domain.AccountUpdater(a.accountNumber, a.balance) FROM  BankAccount a",
                        AccountUpdater.class)
                .getResultList();

        List<AccountUpdater> updates = interestPool.invoke(new InterestAccrualUpdateTask(balances));

        for (AccountUpdater acc : updates) {
            em.createQuery("UPDATE BankAccount  a SET a.balance = :bal WHERE a.id =: accNum")
                    .setParameter("bal", acc.getBalance())
                    .setParameter("accNum", acc.getAccountNumber())
                    .executeUpdate();
        }

        log.info("После начисления начисления {}", updates.get(0).getBalance());
    }
}
