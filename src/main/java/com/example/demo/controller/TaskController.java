package com.example.demo.controller;

import com.example.demo.service.TaskManager;
import com.example.demo.entityDB.TaskEntity;
import com.example.demo.entityDB.TaskStatus;
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
    public ResponseEntity<Long> createTask(@RequestBody TaskEntity task) {
        Long taskId = taskManager.createAndScheduleTask(task);
        return ResponseEntity.ok(taskId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelTask(@PathVariable Long id) {
        taskManager.cancelTask(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<TaskStatus> getTaskStatus(@PathVariable Long id) {
        TaskStatus status = taskManager.getTaskStatus(id);
        if (status == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(status);
    }
}
