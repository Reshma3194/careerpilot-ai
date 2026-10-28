package com.careerpilot.repository;

import com.careerpilot.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByUserEmailOrderByUploadedAtDesc(String email);

    Optional<Resume> findByIdAndUserEmail(Long id, String email);

    Optional<Resume> findFirstByUserEmailOrderByUploadedAtDesc(String email);
}
