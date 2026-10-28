package com.careerpilot.repository;

import com.careerpilot.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findByUserEmailOrderByUpdatedAtDesc(String email);

    List<JobApplication> findByUserEmailAndStatusOrderByUpdatedAtDesc(String email, JobApplication.Status status);

    Optional<JobApplication> findByIdAndUserEmail(Long id, String email);

    Optional<JobApplication> findByJobIdAndUserEmail(Long jobId, String email);

    boolean existsByJobIdAndUserEmail(Long jobId, String email);

    void deleteByJobId(Long jobId);

    @Query("select a.status, count(a) from JobApplication a where a.user.email = :email group by a.status")
    List<Object[]> countByStatus(@Param("email") String email);
}
