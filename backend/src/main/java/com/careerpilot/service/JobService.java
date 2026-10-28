package com.careerpilot.service;

import com.careerpilot.dto.JobRequest;
import com.careerpilot.entity.Job;
import com.careerpilot.entity.User;
import com.careerpilot.exception.ResourceNotFoundException;
import com.careerpilot.repository.ApplicationRepository;
import com.careerpilot.repository.JobRepository;
import com.careerpilot.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;

    public JobService(JobRepository jobRepository, UserRepository userRepository,
                      ApplicationRepository applicationRepository) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
    }

    @Transactional
    public Job create(String email, JobRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Job job = new Job();
        job.setUser(user);
        apply(job, request);
        return jobRepository.save(job);
    }

    @Transactional(readOnly = true)
    public List<Job> list(String email) {
        return jobRepository.findByUserEmailOrderByCreatedAtDesc(email);
    }

    @Transactional(readOnly = true)
    public Job get(String email, Long id) {
        return jobRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    @Transactional
    public Job update(String email, Long id, JobRequest request) {
        Job job = get(email, id);
        apply(job, request);
        return jobRepository.save(job);
    }

    @Transactional
    public void delete(String email, Long id) {
        Job job = get(email, id);
        applicationRepository.deleteByJobId(job.getId());
        jobRepository.delete(job);
    }

    private void apply(Job job, JobRequest request) {
        job.setTitle(request.title().trim());
        job.setCompany(request.company().trim());
        job.setLocation(blankToNull(request.location()));
        job.setDescription(blankToNull(request.description()));
        job.setJobUrl(blankToNull(request.jobUrl()));
    }

    private static String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
