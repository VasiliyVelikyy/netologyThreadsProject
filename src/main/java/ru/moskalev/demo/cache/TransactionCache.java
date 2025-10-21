package ru.moskalev.demo.cache;

import ru.moskalev.demo.data.TransferGeneratorService;

import java.util.Collection;

public interface TransactionCache {

    void addTransaction(String accNum, TransferGeneratorService.TransferOperation op);
    Collection<TransferGeneratorService.TransferOperation> getLastTransactions(String accNum);
}
