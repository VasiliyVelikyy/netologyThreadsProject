package ru.moskalev.demo.controller.demostrate;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.moskalev.demo.data.DataInitializer;
import ru.moskalev.demo.domain.entity.BankAccount;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/degradation")
@RequiredArgsConstructor
public class DegradationDemonstrationController {

    private final DataInitializer dataInitializer;

    private List<BankAccount> clientList;
    private Map<String, BankAccount> clientMap;
    private List<BankAccount> sortedList;
    private final Path SLOW_LOG_FILE = Paths.get("slow_io_sync.log").toAbsolutePath();
    private final Path FAST_LOG_FILE = Paths.get("fast_io.log").toAbsolutePath();

    //@Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void initCache() {
        clientList = dataInitializer.getAccounts();
        this.clientMap = clientList.stream()
                .collect(Collectors.toConcurrentMap(
                        BankAccount::getAccountNumber,
                        Function.identity(),
                        (a, b) -> a
                ));

        log.info("Cache inizialize with {} accsize", clientList.size());
        sortedListFoAlg();
    }

    private void sortedListFoAlg() {
        sortedList=new ArrayList<>(clientList);
        Collections.sort(sortedList,
                Comparator.comparing(BankAccount::getAccountNumber));
    }


    @GetMapping("/cpu")
    public String burnCpu() {
        long sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += i * i;
        }
        return STR."done: \{sum}";
    }

    @GetMapping("/slow-io")
    public String slowIo() throws IOException {
        try (RandomAccessFile ref = new RandomAccessFile(SLOW_LOG_FILE.toFile(), "rw")) {
            ref.seek(ref.length());
            String entry = "Sync log entry at" + System.currentTimeMillis() + "\n";
            ref.write(entry.getBytes());

            ref.getChannel().force(true);
        }
        return "ok";
    }

    @GetMapping("/fast-io")
    public String fastIo() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FAST_LOG_FILE.toFile(), true))) {
            writer.write("Fast log at " + System.currentTimeMillis() + "\n");
        }
        return "ok";
    }

    @PostMapping("/slow-alg-find-client")
    public ResponseEntity<BankAccount> findClientSlow(@RequestBody Map<String, String> requestParams) {
        String accNum = requestParams.get("accountNumber");

        for (var client : clientList) {
            if (client.getAccountNumber().equals(accNum)) {
                log.info("client found with acc num,={}", accNum);
                return ResponseEntity.ok(client);
            }
        }
        log.warn("client not found with accNum={}", accNum);
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/fast-alg-find-client")
    public ResponseEntity<BankAccount> findClientFast(@RequestBody Map<String, String> requestParams) {
        String accNum = requestParams.get("accountNumber");
        var client = clientMap.get(accNum);
        if (client != null) {
            log.info("client found with acc num,={}", accNum);
            return ResponseEntity.ok(client);
        }
        log.warn("client not found with accNum={}", accNum);
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/middle-alg-find-client")
    public ResponseEntity<BankAccount> findClientMiddle(@RequestBody Map<String, String> requestParams) {
        String accNum = requestParams.get("accountNumber");

        int index = Collections.binarySearch(sortedList,
                new BankAccount(accNum, 0),
                Comparator.comparing(BankAccount::getAccountNumber));

        if (index >= 0) {
            BankAccount client = sortedList.get(index);
            log.info("client found with acc num,={}", accNum);
            return ResponseEntity.ok(client);
        }
        log.warn("client not found with accNum={}", accNum);
        return ResponseEntity.notFound().build();
    }
}
