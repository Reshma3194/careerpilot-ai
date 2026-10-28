package com.careerpilot.repository;

import com.careerpilot.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByUserEmailOrderByCreatedAtDesc(String email);

    Optional<Job> findByIdAndUserEmail(Long id, String email);
}
