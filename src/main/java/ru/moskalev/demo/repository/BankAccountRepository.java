package ru.moskalev.demo.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.moskalev.demo.domain.entity.BankAccount;

import java.util.List;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, String> {

    List<BankAccount> findByBalanceLessThan(double threshold);

    List<BankAccount> findByBalanceGreaterThan(double threshold);

    @Query("SELECT b FROM BankAccount b " +
            "LEFT JOIN FETCH b.accountInfo " +
            "LEFT JOIN FETCH b.accountRisk")
    List<BankAccount> findAllWithDetails();
}
