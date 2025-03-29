package com.job.interview.blog.repository;

import com.job.interview.blog.model.BlogCategory;
import com.job.interview.blog.model.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    @Query("SELECT b FROM BlogPost b WHERE (:id IS NOT NULL AND b.id = :id)")
    Optional<BlogPost> findByIdOrReturnNull(@Param("id") Long id);
    @Query("SELECT b FROM BlogPost b WHERE (:category IS NULL OR b.category = :category)")
    Page<BlogPost> findAllByCategory(@Param("category") BlogCategory category, Pageable pageable);
}