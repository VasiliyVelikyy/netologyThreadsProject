package ru.moskalev.demo.service.jpaproblem;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.moskalev.demo.domain.entity.AccountInfo;
import ru.moskalev.demo.domain.entity.AccountRisk;
import ru.moskalev.demo.domain.entity.BankAccount;
import ru.moskalev.demo.repository.BankAccountRepository;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class BatchInsertService {
    AtomicInteger objectCounts = new AtomicInteger(0);
    private final BankAccountRepository bankAccountRepository;

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private int batchValue;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void importAcc(int count) {
        for (int i = 1; i <= count; i++) {
            String accNum = "BATCH " + objectCounts.getAndIncrement();
            var acc = new BankAccount(accNum, 10000.0);

            AccountInfo accountInfo = new AccountInfo();
            accountInfo.setCurrency("USD");
            accountInfo.setStatus("ACTIVE");
            accountInfo.setCreatedAt(LocalDateTime.now());
            accountInfo.setLastTransactionAt(LocalDateTime.now());
            accountInfo.setBankAccount(acc);

            AccountRisk accountRisk = new AccountRisk();
            accountRisk.setRiskScore(i % 100);
            accountRisk.setLastCredit(LocalDateTime.now());
            accountRisk.setBankAccount(acc);

            acc.setAccountInfo(accountInfo);
            acc.setAccountRisk(accountRisk);

            bankAccountRepository.save(acc);
            if (i % batchValue == 0) {
                entityManager.flush();
                entityManager.clear();
            }

        }
    }
}
