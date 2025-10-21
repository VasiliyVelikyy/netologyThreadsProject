package ru.moskalev.demo.cache;

import org.springframework.stereotype.Service;
import ru.moskalev.demo.data.TransferGeneratorService;

import java.util.*;

@Service
public class UnsafeTransactionCache implements TransactionCache {
    private final Map<String, Deque<TransferGeneratorService.TransferOperation>> cache = new HashMap<>();

    public void addTransaction(String accountNumber, TransferGeneratorService.TransferOperation tx) {
        cache.compute(accountNumber, (key, deque) -> {
            if (deque == null) {
                deque = new ArrayDeque<>(10);
            }
            deque.offerFirst(tx);
            while (deque.size() > 10) {
                deque.pollLast();
            }
            return deque;
        });
    }

    public List<TransferGeneratorService.TransferOperation> getLastTransactions(String accountNumber) {
        Deque<TransferGeneratorService.TransferOperation> deque = cache.get(accountNumber);
        return deque != null ? new ArrayList<>(deque) : Collections.emptyList();
    }
}