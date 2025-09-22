package ru.moskalev.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.moskalev.demo.service.exproblem.StreamTransferService;

@RestController
public class StreamTransferController {

    private final StreamTransferService streamTransferService;

    public StreamTransferController(StreamTransferService streamTransferService) {
        this.streamTransferService = streamTransferService;
    }

    @GetMapping("/start-stream")
    public String startStream() {
        return streamTransferService.startStream();
    }

    @GetMapping("/start-parallel-stream")
    public String startParallelStream() {
        return streamTransferService.startParallelStream();
    }

    @GetMapping("/start-parallel-stream/block")
    public String startParallelStreamBlock() {
        return streamTransferService.startParallelStreamBlock();
    }


    @GetMapping("/start-fork-join-parallel-stream")
    public String startForkJoinPoolParallelStream() {
        return streamTransferService.startForkJoinPoolParallelStream();
    }
}