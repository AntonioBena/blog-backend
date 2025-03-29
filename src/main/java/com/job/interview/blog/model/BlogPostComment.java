package com.job.interview.blog.model;

import com.job.interview.blog.model.user.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "blog_post_comment")
public class BlogPostComment extends AuditingModel implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;
    @NotNull(message = "Comment is mandatory")
    @NotEmpty(message = "Comment is mandatory")
    private String comment;
    @NotNull
    @OneToOne
    private UserEntity user;
}