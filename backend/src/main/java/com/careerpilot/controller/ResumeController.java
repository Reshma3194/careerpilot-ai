package com.careerpilot.controller;

import com.careerpilot.dto.ApiResponse;
import com.careerpilot.entity.Resume;
import com.careerpilot.service.ResumeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Resume>> upload(Authentication auth,
                                                      @RequestPart("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Resume uploaded", resumeService.upload(auth.getName(), file)));
    }

    @GetMapping
    public ApiResponse<List<Resume>> list(Authentication auth) {
        return ApiResponse.ok(resumeService.list(auth.getName()));
    }

    @GetMapping("/{id}")
    public ApiResponse<Resume> get(Authentication auth, @PathVariable Long id) {
        return ApiResponse.ok(resumeService.get(auth.getName(), id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(Authentication auth, @PathVariable Long id) {
        resumeService.delete(auth.getName(), id);
        return ApiResponse.ok("Resume deleted", null);
    }
}
