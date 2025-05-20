package com.example.demo.controller;

import com.example.demo.TaskRequest;
import com.example.demo.service.TaskManager;
import com.example.demo.entity.TaskStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskManager taskManager;

    @Autowired
    public TaskController(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @PostMapping
    public ResponseEntity<Long> createTask(@RequestBody TaskRequest dto) {
        Long taskId = taskManager.createAndSchedule(dto.toEntity());
        return ResponseEntity.ok(taskId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelTask(@PathVariable Long id,
                                           @RequestParam String category) {
        boolean isOk = taskManager.cancel(id, category);
        return isOk ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<TaskStatus> statusTask(@PathVariable Long id,
                                                    @RequestParam String category) {
        TaskStatus status = taskManager.getStatus(id, category);
        return status == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(status);
    }
}
