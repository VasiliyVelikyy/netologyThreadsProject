package ru.moskalev.demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import ru.moskalev.demo.generators.TransferGeneratorService;
import ru.moskalev.demo.service.CacheableService;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
public class LastTransactionListController {
    private final CacheableService cacheableService;

    @GetMapping("/api/last-transaction/{accNum}")
    public Collection<TransferGeneratorService.TransferOperation> getLastTransaction(@PathVariable String accNum){
        return cacheableService.getTransfersByAcc(accNum);
    }
}
