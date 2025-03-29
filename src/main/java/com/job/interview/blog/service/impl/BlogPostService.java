package com.job.interview.blog.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.job.interview.blog.configuration.ApplicationProperties;
import com.job.interview.blog.model.BlogCategory;
import com.job.interview.blog.model.BlogPost;
import com.job.interview.blog.model.BlogPostComment;
import com.job.interview.blog.model.dto.BlogPostDto;
import com.job.interview.blog.model.dto.CommentDto;
import com.job.interview.blog.model.dto.response.PageResponse;
import com.job.interview.blog.model.user.UserEntity;
import com.job.interview.blog.model.user.UserRole;
import com.job.interview.blog.repository.BlogPostRepository;
import com.job.interview.blog.repository.CommentsRepository;
import com.job.interview.blog.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.LocalDate;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class BlogPostService extends FileProcessor {
    private final ApplicationProperties appProperties;
    private final BlogPostRepository blogPostRepository;
    private final CommentsRepository commentsRepository;
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
//        var user = UserEntity.builder() //TODO user will be loaded from security context
//                .firstName("user")
//                .lastName("user l")
//                .email("toni@gmail.com")
//                .password("1234564")
//                .role(UserRole.WRITER)
//                .accountLocked(false)
//                .enabled(true)
//                .build();
//        var savedU = userRepository.save(user);

        blogPost.setPostOwner(userRepository.findAll().getFirst());
        blogPost.setPublishedAt(LocalDate.now());

        var savedBlogPost = blogPostRepository.save(blogPost);
        var savedHtml = saveToDisc(htmlFile, savedBlogPost.getId());

        savedBlogPost.setHtmlContentPath(savedHtml);
        log.info("Created Blog Post: {}", blogPost);
        blogPostRepository.save(blogPost);
    }

    @Transactional
    private void updateBlogPost(BlogPost blogPost, MultipartFile htmlFile) {
        blogPost.setLikedBy(blogPost.getLikedBy());
        var savedHtml = saveToDisc(htmlFile, blogPost.getId());

        blogPost.setHtmlContentPath(savedHtml);
        log.info("Updated Blog Post: {}", blogPost);
        blogPostRepository.save(blogPost);
    }

    public PageResponse<?> getAllDisplayablePosts(int page, int size, BlogCategory category){
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        Page<BlogPost> cases = blogPostRepository.findAllByCategory(category, pageable);
        log.info("Get all cases by param: {}", cases);
        return mapPostsToPageResponse(cases);
    }

    private PageResponse<BlogPostDto> mapPostsToPageResponse(Page<BlogPost> posts) {
        List<BlogPostDto> casesDto = posts
                .stream()
                .map(p -> mapper.map(p, BlogPostDto.class))
                .toList();
        log.info("Blog posts number of total elements: {}, blog posts total elements - long {}",
                posts.getNumberOfElements(), posts.getTotalElements());
        return new PageResponse<>(
                casesDto,
                posts.getNumber(),
                posts.getSize(),
                posts.getTotalElements(),
                posts.getTotalPages(),
                posts.isLast(),
                posts.isFirst()
        );
    }

    public BlogPostDto getBlogPostById(Long id){
        var blogPost = blogPostRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("We did not found any blog post with that id"));
        return mapper.map(blogPost, BlogPostDto.class);
    }

    public Resource download(Long id){
        var blogPost = blogPostRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Not found"));

        if(blogPost.getHtmlContentPath().isEmpty()){
            throw new RuntimeException("Content path of blog post is not found");
        }
        return downloadFile(blogPost.getHtmlContentPath());
    }

    public void deleteBlogPost(Long id){
        var blogPost = blogPostRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Not found"));
        blogPostRepository.delete(blogPost);
    }

    public long likeUnlikeBlogPost(Long id){
        var blogPost = blogPostRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Not found"));
        //TODO get liked user from security context

        //var likedBy = blogPost.getLikedBy().add(foundUser.getFullName());
        // check if user already is on the list, if it is then unlike and remove from list

        var likesCount = blogPost.getLikeCount() + 1;

        blogPostRepository.save(blogPost);

        return likesCount;
    }

    public CommentDto commentBlogPost(CommentDto commentDto, Long id){
        var foundComments = commentsRepository.findAllCommentsByBlogPostId(id);
        //TODO get user from security context
        var foundPost = blogPostRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("not found"));

        var user = UserEntity.builder() //TODO user will be loaded from security context
                .firstName("user")
                .lastName("user l")
                .email("toni@gmail.com")
                .password("1234564")
                .role(UserRole.WRITER)
                .accountLocked(false)
                .enabled(true)
                .build();
        //TODO add comment repository and persist comment before adding to post

        var comment = mapper.map(commentDto, BlogPostComment.class);
        comment.setBlogPost(foundPost);
        foundComments.add(comment);
        commentsRepository.save(comment);
        return mapper.map(foundComments, CommentDto.class);
    }
}