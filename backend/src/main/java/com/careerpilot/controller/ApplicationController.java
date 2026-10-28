package com.careerpilot.controller;

import com.careerpilot.dto.ApiResponse;
import com.careerpilot.dto.ApplicationRequest;
import com.careerpilot.dto.ApplicationUpdateRequest;
import com.careerpilot.entity.JobApplication;
import com.careerpilot.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JobApplication>> create(Authentication auth,
                                                              @Valid @RequestBody ApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Application added", applicationService.create(auth.getName(), request)));
    }

    @GetMapping
    public ApiResponse<List<JobApplication>> list(Authentication auth,
                                                  @RequestParam(required = false) JobApplication.Status status) {
        return ApiResponse.ok(applicationService.list(auth.getName(), status));
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Long>> stats(Authentication auth) {
        return ApiResponse.ok(applicationService.stats(auth.getName()));
    }

    @GetMapping("/{id}")
    public ApiResponse<JobApplication> get(Authentication auth, @PathVariable Long id) {
        return ApiResponse.ok(applicationService.get(auth.getName(), id));
    }

    @PatchMapping("/{id}")
    public ApiResponse<JobApplication> update(Authentication auth, @PathVariable Long id,
                                              @RequestBody ApplicationUpdateRequest request) {
        return ApiResponse.ok("Application updated", applicationService.update(auth.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(Authentication auth, @PathVariable Long id) {
        applicationService.delete(auth.getName(), id);
        return ApiResponse.ok("Application deleted", null);
    }
}
