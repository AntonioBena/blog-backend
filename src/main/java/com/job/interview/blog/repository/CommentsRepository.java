package com.job.interview.blog.repository;

import com.job.interview.blog.model.BlogPostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface CommentsRepository extends JpaRepository<BlogPostComment, Long> {
    Set<BlogPostComment> findAllCommentsByBlogPostId(Long id);
}