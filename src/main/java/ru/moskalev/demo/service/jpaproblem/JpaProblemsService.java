package ru.moskalev.demo.service.jpaproblem;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.moskalev.demo.domain.dto.BankAccountCreateDto;
import ru.moskalev.demo.domain.dto.BankAccountDto;
import ru.moskalev.demo.domain.entity.AccountInfo;
import ru.moskalev.demo.domain.entity.AccountRisk;
import ru.moskalev.demo.domain.entity.BankAccount;
import ru.moskalev.demo.repository.BankAccountRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JpaProblemsService {
    private final BankAccountRepository bankAccountRepository;

    public List<BankAccountDto> getAccountsWithNPlusOne() {
        List<BankAccount> accounts = bankAccountRepository.findAll();
        return getBankAccountDtos(accounts);
    }

    public List<BankAccountDto> getAccountsWithoutNPlusOne() {
        List<BankAccount> accounts = bankAccountRepository.findAllWithDetails();
        return getBankAccountDtos(accounts);
    }

    private List<BankAccountDto> getBankAccountDtos(List<BankAccount> accounts) {
        return accounts.stream()
                .map(this::mapEntityToDto).toList();
    }

    private BankAccountDto mapEntityToDto(BankAccount account) {
        return new BankAccountDto(account.getAccountNumber(),
                account.getBalance(),
                account.getAccountInfo() != null ? account.getAccountInfo().getCurrency() : null,
                account.getAccountInfo() != null ? account.getAccountInfo().getStatus() : null,
                account.getAccountRisk() != null ? account.getAccountRisk().getRiskScore() : null);
    }

    public BankAccountCreateDto createAccountsWithInfo(BankAccountCreateDto dto) {
        BankAccount acc = new BankAccount(dto.getAccountNumber(), dto.getBalance());

        AccountInfo accountInfo = new AccountInfo();
        accountInfo.setCurrency(dto.getCurrency());
        accountInfo.setStatus(dto.getStatus());
        accountInfo.setCreatedAt(dto.getCreatedAt());
        accountInfo.setLastTransactionAt(dto.getLastTransactionAt());
        accountInfo.setBankAccount(acc);

        AccountRisk accountRisk = new AccountRisk();
        accountRisk.setRiskScore(accountRisk.getRiskScore());
        accountRisk.setLastCredit(dto.getLastCredit());
        accountRisk.setBankAccount(acc);

        acc.setAccountInfo(accountInfo);
        acc.setAccountRisk(accountRisk);
        bankAccountRepository.save(acc);

        return dto;
    }
}
