package ru.moskalev.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.moskalev.demo.domain.BankAccount;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, String> {
}
