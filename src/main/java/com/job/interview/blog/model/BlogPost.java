package com.job.interview.blog.model;

import com.job.interview.blog.model.user.UserEntity;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "blog_post")
public class BlogPost implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blog_post_id", updatable = false)
    private Long id;
    private String title;
    @NotEmpty(message = "Short Content is mandatory")
    @NotBlank(message = "Short Content is mandatory")
    @Column(name = "description", columnDefinition = "text")
    private String shortContent;
    private String shortContentImageUrl;
    @Enumerated(EnumType.STRING)
    private BlogCategory category;

    @Column(name = "html_content_path")
    private String htmlContentPath;

    private long likeCount;
    private long commentCount;
    private long viewCount;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "post_id")
    private Set<BlogPostComment> comments;
    private Set<String> likedBy;

    @Valid
    @ManyToOne(optional = false)
    @JoinColumn(name = "post_id")
    private UserEntity postOwner;
}