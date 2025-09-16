package ru.moskalev.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.moskalev.demo.service.TransferProblemService;

@RestController
public class ProblemTransferController {
    private final TransferProblemService transferProblemService;

    public ProblemTransferController(TransferProblemService transferProblemService) {
        this.transferProblemService = transferProblemService;
    }

    @GetMapping("/race-condition")
    public String processRaceCondition(){
        return transferProblemService.transferRaceCondition();
    }

    @GetMapping("/race-condition/sync")
    public String processRaceConditionSync(){
        return transferProblemService.transferRaceConditionSync();
    }

    @GetMapping("/transfer-deadlock")
    public String processDeadlock(){
        return transferProblemService.transferDeadlock();
    }

    @GetMapping("/transfer-livelock")
    public String processLivelock() throws InterruptedException {
        return transferProblemService.transferLivelockWithMaxAttempt();
    }
}
