package com.example.demo.controller;

import com.example.demo.dto.TaskRequest;
import com.example.demo.service.TaskLifecycleService;
import com.example.demo.entity.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskLifecycleService taskLifecycleService;

    @Autowired
    public TaskController(TaskLifecycleService taskLifecycleService) {
        this.taskLifecycleService = taskLifecycleService;
    }

    @PostMapping
    public ResponseEntity<Long> createTask(@RequestBody TaskRequest dto) {
        Long id = taskLifecycleService.create(dto.toEntity());

        taskLifecycleService.initCategoryPool(dto.getCategory());

        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelTask(@PathVariable Long id,
                                           @RequestParam String category) {
        boolean isOk = taskLifecycleService.cancel(id, category);
        return isOk ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<TaskStatus> getStatusTask(@PathVariable Long id,
                                                    @RequestParam String category) {
        TaskStatus status = taskLifecycleService.getStatus(id, category);
        return status == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(status);
    }

    @PostMapping("/init-pool")
    public ResponseEntity<String> initPool(@RequestParam String category) {
        taskLifecycleService.initCategoryPool(category);
        return ResponseEntity.ok("Pool initialized for category: " + category);
    }

}
