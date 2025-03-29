package com.job.interview.blog.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.job.interview.blog.configuration.ApplicationProperties;
import com.job.interview.blog.model.BlogPost;
import com.job.interview.blog.model.dto.BlogPostDto;
import com.job.interview.blog.model.user.UserEntity;
import com.job.interview.blog.model.user.UserRole;
import com.job.interview.blog.repository.BlogPostRepository;
import com.job.interview.blog.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Log4j2
@Service
@RequiredArgsConstructor
public class BlogPostProcessor extends FileProcessor {
    private final ApplicationProperties appProperties;
    private final BlogPostRepository blogPostRepository;
    private final ModelMapper mapper;

    @Autowired
    private UserRepository userRepository; //TODO remove repo

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void saveAndPublish(String blogPostJson, MultipartFile htmlFile) throws JsonProcessingException {
        var blogPostDto = objectMapper.readValue(blogPostJson, BlogPostDto.class);

        var blogPostEntity = mapper.map(blogPostDto, BlogPost.class);

        blogPostRepository.findByIdOrReturnNull(blogPostDto.getId())
                .ifPresentOrElse(
                        existingPost -> updateBlogPost(existingPost, htmlFile),
                        () -> createBlogPost(blogPostEntity, htmlFile)
                );
    }

    @Transactional
    private void createBlogPost(BlogPost blogPost, MultipartFile htmlFile) { //TODO user must be present
        var user = UserEntity.builder() //TODO user will be loaded from security context
                .firstName("user")
                .lastName("user l")
                .email("toni@gmail.com")
                .password("1234564")
                .role(UserRole.WRITER)
                .accountLocked(false)
                .enabled(true)
                .build();
        var savedU = userRepository.save(user);

        blogPost.setPostOwner(savedU);

        var savedBlogPost = blogPostRepository.save(blogPost);
        var savedHtml = saveToDisc(htmlFile, savedBlogPost.getId());

        savedBlogPost.setHtmlContentPath(savedHtml);
        log.info("Created Blog Post: {}", blogPost);
        blogPostRepository.save(blogPost);
    }

    @Transactional
    private void updateBlogPost(BlogPost blogPost, MultipartFile htmlFile) {
        blogPost.setComments(blogPost.getComments());
        blogPost.setLikedBy(blogPost.getLikedBy());
        var savedHtml = saveToDisc(htmlFile, blogPost.getId());

        blogPost.setHtmlContentPath(savedHtml);
        log.info("Updated Blog Post: {}", blogPost);
        blogPostRepository.save(blogPost);
    }
}