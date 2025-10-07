package ru.moskalev.demo.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.moskalev.demo.domain.BankAccount;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, String> {

    default BankAccount getAccount(String accountNumber) {
        Optional<BankAccount> account = findById(accountNumber);
        return account
                .orElseThrow(() -> new RuntimeException("Счет не найден " + accountNumber));

    }

    List<BankAccount> findByBalanceLessThan(double threshold);
    List<BankAccount> findByBalanceGreaterThan(double threshold);
}
