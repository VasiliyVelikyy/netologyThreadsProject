package ru.moskalev.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.moskalev.demo.service.ProcessService;

@RestController
public class TransferController {

    private final ProcessService processService;

    public TransferController(ProcessService processService) {
        this.processService = processService;
    }

    @GetMapping("/hello")
    public String hello() {
        return "Привет!";
    }

    @GetMapping("/start-demon")
    public String startDemon() {
        return processService.processStartDemon();
    }

    @GetMapping("/process-runnable")
    public String processRunnable() {
        return processService.processRunnable();
    }

    @GetMapping("/process-waiting")
    public String processWaiting() {
        return processService.processWaiting();
    }


    @GetMapping("/process-blocked")
    public String processBlocked() {
        return processService.processBlocked();
    }

    @GetMapping("/process-park")
    public String processPark() {
        return processService.processPark();
    }

}
