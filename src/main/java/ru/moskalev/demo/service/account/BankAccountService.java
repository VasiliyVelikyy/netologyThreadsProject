package ru.moskalev.demo.service.account;


import com.github.benmanes.caffeine.cache.Cache;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;
import ru.moskalev.demo.domain.ClientBalanceDto;
import ru.moskalev.demo.domain.entity.BankAccount;
import ru.moskalev.demo.repository.BankAccountRepository;
import ru.moskalev.demo.repository.ClientBalanceRepository;

import java.util.List;

@Slf4j
@Service

public class BankAccountService {
    private final BankAccountRepository bankAccountRepository;
    private final Cache<String, ClientBalanceDto> fallBackCache;
    private final ClientBalanceRepository clientBalanceRepository;

    public BankAccountService(BankAccountRepository bankAccountRepository,
                              CacheManager cacheManager,
                              ClientBalanceRepository clientBalanceRepository) {
        Cache rawCache = ((CaffeineCache) cacheManager.getCache("clients")).getNativeCache();
        this.fallBackCache = (Cache<String, ClientBalanceDto>) rawCache;
        this.bankAccountRepository = bankAccountRepository;
        this.clientBalanceRepository = clientBalanceRepository;

    }

    @Cacheable("clients")
    public List<ClientBalanceDto> getClientBalances() {
        var accounts = bankAccountRepository.findAll();
        return accounts.stream()
                .map(acc -> new ClientBalanceDto(
                        acc.getAccountNumber(),
                        acc.getBalance()))
                .toList();
    }

    @Transactional
    public ClientBalanceDto updateAcc(String accNum, double balance) {
        BankAccount account = getAccount(accNum);
        account.setBalance(balance);
        return new ClientBalanceDto(account.getAccountNumber(), account.getBalance());
    }


    @Cacheable(value = "clients", unless = "#result.balance<1000")
    public ClientBalanceDto getAccountByNum(String accNum) {
        var acc = getAccount(accNum);
        return new ClientBalanceDto(acc.getAccountNumber(),
                acc.getBalance());
    }

    public BankAccount getAccount(String accNum) {
        return bankAccountRepository
                .findById(accNum)
                .orElseThrow(() -> new RuntimeException("Счет не найден " + accNum));
    }

    @CacheEvict(value = "clients", allEntries = true)
    public void evictCache() {
        log.info("Eviction cache");
    }


    public ClientBalanceDto getClientWithFallback(String id) {
        var cache = fallBackCache.getIfPresent(id);
        if (cache != null) {
            return cache;
        }
        try {
            var acc = bankAccountRepository.findById(id).orElseThrow(() ->
                    new RuntimeException("Acc not found " + id));
            var fresh = new ClientBalanceDto(acc.getAccountNumber(),
                    acc.getBalance());
            fallBackCache.put(id, fresh);
            return fresh;
        } catch (Exception e) {
            throw new RuntimeException("Cache missing");
        }
    }

    public void saveClientBalanceToRedis(String accNum, double balance) {
        var dto = new ClientBalanceDto(accNum, balance);
        clientBalanceRepository.save(dto);
    }
}
