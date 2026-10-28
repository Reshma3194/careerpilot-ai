package com.careerpilot.controller;

import com.careerpilot.dto.ApiResponse;
import com.careerpilot.dto.JobRequest;
import com.careerpilot.entity.Job;
import com.careerpilot.service.JobService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Job>> create(Authentication auth, @Valid @RequestBody JobRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Job saved", jobService.create(auth.getName(), request)));
    }

    @GetMapping
    public ApiResponse<List<Job>> list(Authentication auth) {
        return ApiResponse.ok(jobService.list(auth.getName()));
    }

    @GetMapping("/{id}")
    public ApiResponse<Job> get(Authentication auth, @PathVariable Long id) {
        return ApiResponse.ok(jobService.get(auth.getName(), id));
    }

    @PutMapping("/{id}")
    public ApiResponse<Job> update(Authentication auth, @PathVariable Long id,
                                   @Valid @RequestBody JobRequest request) {
        return ApiResponse.ok("Job updated", jobService.update(auth.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(Authentication auth, @PathVariable Long id) {
        jobService.delete(auth.getName(), id);
        return ApiResponse.ok("Job deleted", null);
    }
}
