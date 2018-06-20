package com.epam.coderunner.controllers;

import com.epam.coderunner.model.TaskRequest;
import com.epam.coderunner.runners.CodeRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
final class FileUploadController {
    private static final Logger LOG = LoggerFactory.getLogger(FileUploadController.class);

    private final CodeRunner runner;

    @Autowired
    public FileUploadController(final CodeRunner runner) {
        this.runner = runner;
    }

    @GetMapping("/")
    public String loadUploadPage(){
        return "uploadForm";
    }

    @PostMapping("/task/{taskId}")
    public @ResponseBody String handleFileUpload(@PathVariable long taskId, @RequestBody TaskRequest code){
            LOG.debug("Running code {}", code.getSource());
            return runner.run(taskId, code.getSource());
    }

}
