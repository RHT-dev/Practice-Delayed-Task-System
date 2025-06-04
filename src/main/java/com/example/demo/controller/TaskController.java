package com.example.demo.controller;

import com.example.demo.dto.TaskRequest;
import com.example.demo.entity.TaskEntity;
import com.example.demo.repository.DynamicTaskTableDao;
import com.example.demo.scheduler.TaskDispatcher;
import com.example.demo.service.TaskLifecycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskLifecycleService taskLifecycleService;
    private final TaskDispatcher dispatcher;
    private final DynamicTaskTableDao dao;

    @Autowired
    public TaskController(TaskLifecycleService taskLifecycleService,
                          TaskDispatcher dispatcher,
                          DynamicTaskTableDao dao) {
        this.taskLifecycleService = taskLifecycleService;
        this.dispatcher = dispatcher;
        this.dao = dao;
    }

    @PostMapping
    public ResponseEntity<Long> createTask(@RequestBody TaskRequest dto) {
        TaskEntity entity = dto.toEntity();
        Long id = taskLifecycleService.create(entity);
        entity.setId(id);

        taskLifecycleService.initCategoryPool(entity.getCategory());
        dispatcher.initCategoryDispatcher(entity.getCategory());

        dispatcher.enqueueTask(entity);

        return ResponseEntity.ok(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelTask(@PathVariable Long id,
                                           @RequestParam String category) {
        boolean isOk = taskLifecycleService.cancel(id, category);
        return isOk ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskEntity> getTask(@PathVariable Long id,
                                              @RequestParam String category) {
        TaskEntity task = dao.getTaskById(id, category);
        return task == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(task);
    }

    @PostMapping("/reload")
    public ResponseEntity<String> reloadCategory(@RequestParam String category) {
        List<TaskEntity> tasks = dao.fetchAllConsidered(category);
        tasks.forEach(dispatcher::enqueueTask);
        return ResponseEntity.ok("Reloaded " + tasks.size() + " tasks for category " + category);
    }

    @PostMapping("/init-pool")
    public ResponseEntity<String> initPool(@RequestParam String category) {
        taskLifecycleService.initCategoryPool(category);
        return ResponseEntity.ok("Pool initialized for category: " + category);
    }

}
