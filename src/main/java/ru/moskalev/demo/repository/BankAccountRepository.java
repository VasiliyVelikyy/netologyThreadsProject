package ru.moskalev.demo.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.moskalev.demo.domain.account.BankAccount;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, String> {

    List<BankAccount> findByBalanceLessThan(double threshold);
    List<BankAccount> findByBalanceGreaterThan(double threshold);
}
