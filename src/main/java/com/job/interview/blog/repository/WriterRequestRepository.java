package com.job.interview.blog.repository;

import com.job.interview.blog.model.WriterRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WriterRequestRepository extends JpaRepository<WriterRequest, Long> {
    Optional<WriterRequest> findByUserId(Long userId);
}