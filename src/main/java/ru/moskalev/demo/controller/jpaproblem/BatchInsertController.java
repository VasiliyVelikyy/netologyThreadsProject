package ru.moskalev.demo.controller.jpaproblem;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.moskalev.demo.service.jpaproblem.BatchInsertService;

@RestController
@RequiredArgsConstructor
public class BatchInsertController {
    private final BatchInsertService batchInsertService;

    @GetMapping("import/batch")
    public ResponseEntity<String> importAcc(@RequestParam(defaultValue = "1000") int count) {
        batchInsertService.importAcc(count);
        return ResponseEntity.ok("Imported " + count);
    }
}
