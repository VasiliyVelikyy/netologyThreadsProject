package ru.moskalev.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.moskalev.demo.service.SpeedTestSyncAndAtomicService;

@RestController
public class SpeedTestSyncAndAtomicController {
    private final SpeedTestSyncAndAtomicService speedTestSyncAndAtomicService;

    public SpeedTestSyncAndAtomicController(SpeedTestSyncAndAtomicService speedTestSyncAndAtomicService) {
        this.speedTestSyncAndAtomicService = speedTestSyncAndAtomicService;
    }

    @GetMapping("/test-speed-sync-atomic")
    public String processSpeedTestAtomic() throws InterruptedException {
        return speedTestSyncAndAtomicService.processSpeedTestAtomic();
    }
}
