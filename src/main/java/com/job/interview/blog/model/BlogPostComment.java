package com.job.interview.blog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.job.interview.blog.model.user.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

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
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "blog_post_id", nullable = false)
    private BlogPost blogPost;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "comment_post_id", nullable = false)
    private UserEntity user;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BlogPostComment that = (BlogPostComment) o;
        return Objects.equals(id, that.id) && Objects.equals(comment, that.comment) && Objects.equals(blogPost, that.blogPost) && Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, comment, blogPost, user);
    }

    @Override
    public String toString() {
        return "BlogPostComment{" +
                "id=" + id +
                ", comment='" + comment + '\'' +
                ", blogPost=" + blogPost +
                ", user=" + user +
                '}';
    }
}