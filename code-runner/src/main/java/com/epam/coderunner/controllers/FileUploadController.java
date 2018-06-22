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
final class FileUploadController {
    private static final Logger LOG = LoggerFactory.getLogger(FileUploadController.class);

    private final CodeRunner runner;

    @Autowired
    public FileUploadController(final CodeRunner runner) {
        this.runner = runner;
    }

    @PostMapping("/task/{taskId}")
    public @ResponseBody Mono<String> handleFileUpload(@PathVariable long taskId, @RequestBody TaskRequest code){
            LOG.debug("Running code {}", code.getSource());
            return runner.run(taskId, code.getSource()).map(TestingStatus::toJson);
    }

}
