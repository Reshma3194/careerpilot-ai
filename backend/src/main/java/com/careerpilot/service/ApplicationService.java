package com.careerpilot.service;

import com.careerpilot.dto.ApplicationRequest;
import com.careerpilot.dto.ApplicationUpdateRequest;
import com.careerpilot.entity.Job;
import com.careerpilot.entity.JobApplication;
import com.careerpilot.entity.User;
import com.careerpilot.exception.ConflictException;
import com.careerpilot.exception.ResourceNotFoundException;
import com.careerpilot.repository.ApplicationRepository;
import com.careerpilot.repository.JobRepository;
import com.careerpilot.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public ApplicationService(ApplicationRepository applicationRepository, JobRepository jobRepository,
                              UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public JobApplication create(String email, ApplicationRequest request) {
        Job job = jobRepository.findByIdAndUserEmail(request.jobId(), email)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        if (applicationRepository.existsByJobIdAndUserEmail(job.getId(), email)) {
            throw new ConflictException("This job is already in your tracker");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        JobApplication application = new JobApplication();
        application.setUser(user);
        application.setJob(job);
        application.setStatus(request.status() == null ? JobApplication.Status.SAVED : request.status());
        application.setAppliedDate(request.appliedDate());
        application.setNotes(request.notes());
        stampAppliedDate(application);
        return applicationRepository.save(application);
    }

    @Transactional(readOnly = true)
    public List<JobApplication> list(String email, JobApplication.Status status) {
        return status == null
                ? applicationRepository.findByUserEmailOrderByUpdatedAtDesc(email)
                : applicationRepository.findByUserEmailAndStatusOrderByUpdatedAtDesc(email, status);
    }

    @Transactional(readOnly = true)
    public JobApplication get(String email, Long id) {
        return applicationRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    }

    @Transactional
    public JobApplication update(String email, Long id, ApplicationUpdateRequest request) {
        JobApplication application = get(email, id);
        if (request.status() != null) application.setStatus(request.status());
        if (request.appliedDate() != null) application.setAppliedDate(request.appliedDate());
        if (request.notes() != null) application.setNotes(request.notes());
        stampAppliedDate(application);
        return applicationRepository.save(application);
    }

    @Transactional
    public void delete(String email, Long id) {
        applicationRepository.delete(get(email, id));
    }

    /** Counts per status (all statuses present, zero-filled) plus a total. */
    @Transactional(readOnly = true)
    public Map<String, Long> stats(String email) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (JobApplication.Status s : JobApplication.Status.values()) {
            result.put(s.name(), 0L);
        }
        long total = 0;
        for (Object[] row : applicationRepository.countByStatus(email)) {
            long count = ((Number) row[1]).longValue();
            result.put(((JobApplication.Status) row[0]).name(), count);
            total += count;
        }
        result.put("TOTAL", total);
        return result;
    }

    private void stampAppliedDate(JobApplication application) {
        if (application.getStatus() != JobApplication.Status.SAVED && application.getAppliedDate() == null) {
            application.setAppliedDate(LocalDate.now());
        }
    }
}
