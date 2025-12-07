package ru.moskalev.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.moskalev.demo.domain.Currency;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class CurrencyService {
    private final Cache currencyCache;

    public CurrencyService(CacheManager cacheManager) {
        this.currencyCache = cacheManager.getCache("currencies");
        if (this.currencyCache == null) {
            throw new IllegalArgumentException("Cache not found");
        }
    }

    @Scheduled(fixedRate = 10_000)
    public void refreshCurrencies() {
        var fresh = simulateExternalFetch();
        currencyCache.clear();
        currencyCache.put("all", fresh);
        log.info("Cache refreshe" + fresh);
    }

    private ArrayList<Currency> simulateExternalFetch() {
        Random r = new Random();
        var list = new ArrayList<Currency>();
        list.add(new Currency("USD", 75.0 + r.nextDouble() * 5));
        list.add(new Currency("EUR", 15.0 + r.nextDouble() * 5));
        list.add(new Currency("USD", 25.0 + r.nextDouble()));
        return list;
    }

    public List<CurrencyService> getActtual() {
        var value = currencyCache.get("all", List.class);
        return value != null ? value : List.of();
    }
}
