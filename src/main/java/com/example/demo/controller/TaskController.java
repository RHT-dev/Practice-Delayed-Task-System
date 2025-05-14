package com.example.demo.controller;

import com.example.demo.entityDB.TaskEntity;
import com.example.demo.service.TaskManager;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private TaskManager taskManager;

    public TaskController(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @PostMapping
    public ResponseEntity<TaskEntity> createTask(@RequestBody TaskEntity task) {
        TaskEntity saved = taskManager.createTask(task);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<String> cancelTask(@PathVariable Long id) {
        boolean success = taskManager.cancelTask(id);
        return ResponseEntity.ok(success ? "Cancelled" : "Failed to cancel");
    }

}
