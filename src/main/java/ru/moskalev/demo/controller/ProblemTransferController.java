package ru.moskalev.demo.controller;

import org.springframework.web.bind.annotation.RestController;
import ru.moskalev.demo.service.TransferProblemService;

@RestController
public class ProblemTransferController {
    private final TransferProblemService transferProblemService;

    public ProblemTransferController(TransferProblemService transferProblemService) {
        this.transferProblemService = transferProblemService;
    }
}
