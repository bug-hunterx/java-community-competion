package com.epam.coderunner.controllers;

import com.epam.coderunner.model.TaskRequest;
import com.epam.coderunner.model.TestingStatus;
import com.epam.coderunner.runners.CodeRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
final class TaskController {
    private static final Logger LOG = LoggerFactory.getLogger(TaskController.class);

    private final CodeRunner runner;

    @Autowired
    public TaskController(final CodeRunner runner) {
        this.runner = runner;
    }

    @PostMapping("/task/submit")
    @ResponseBody
    public Mono<String> handleFileUpload(@RequestBody final TaskRequest request) {
        LOG.info("Received task request, taskId={}, userId={}", request.getTaskId(), request.getUserId());
        return runner.run(request).map(TestingStatus::toJson);
    }
}
