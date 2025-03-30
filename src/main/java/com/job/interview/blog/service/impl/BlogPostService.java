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
import com.job.interview.blog.repository.BlogPostRepository;
import com.job.interview.blog.repository.CommentsRepository;
import com.job.interview.blog.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Log4j2
@Service
@RequiredArgsConstructor
public class BlogPostService extends FileProcessor {
    private final ApplicationProperties appProperties;
    private final BlogPostRepository blogPostRepository;
    private final CommentsRepository commentsRepository;
    private final ModelMapper mapper;
    private final UserRepository userRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UserEntity getAuthenticatedUserEntity(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();

        return userRepository.findUserEntityByEmail(currentUserName)
                .orElseThrow(() -> new RuntimeException("User does not exists!"));
    }

    public void saveAndPublish(String blogPostJson, MultipartFile htmlFile) throws JsonProcessingException {
        var authUser = getAuthenticatedUserEntity();

        var blogPostDto = objectMapper.readValue(blogPostJson, BlogPostDto.class);

        var blogPostEntity = mapper.map(blogPostDto, BlogPost.class);

        blogPostRepository.findByIdOrReturnNull(blogPostDto.getId())
                .ifPresentOrElse(
                        existingPost -> updateBlogPost(existingPost, htmlFile),
                        () -> createBlogPost(blogPostEntity, htmlFile, authUser)
                );
    }

    @Transactional
    private void createBlogPost(BlogPost blogPost, MultipartFile htmlFile, UserEntity user) {
        blogPost.setPostOwner(user);
        blogPost.setPublishedAt(LocalDate.now());

        var savedBlogPost = blogPostRepository.save(blogPost);
        var savedHtml = saveToDisc(htmlFile, savedBlogPost.getId());

        savedBlogPost.setHtmlContentPath(savedHtml);
        log.info("Created Blog Post: {}", blogPost);
        blogPostRepository.save(blogPost);
    }

    @Transactional
    private void updateBlogPost(BlogPost blogPost, MultipartFile htmlFile) {
        var authUser = getAuthenticatedUserEntity();

        if(blogPost.getPostOwner() != authUser){
            throw new RuntimeException("You can not edit someone's else blog post!");
        }

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
        var authUser = getAuthenticatedUserEntity();

        var foundBlogPost = blogPostRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Not found"));

        if(foundBlogPost.getPostOwner() != authUser){
            throw new RuntimeException("You can not delete someone's else blog post!");
        }
        blogPostRepository.delete(foundBlogPost);
    }

    public long likeUnlikeBlogPost(Long id){
        var authUser = getAuthenticatedUserEntity();
        var blogPost = blogPostRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Not found"));

        long likesCount = blogPost.getLikeCount();
        Set<String> likedBy = blogPost.getLikedBy();

        if(likedBy
                .contains(authUser.fullName())){
            likedBy.remove(authUser.fullName());
            likesCount = blogPost.getLikeCount() -1;
            blogPost.setLikeCount(likedBy.size());
            blogPostRepository.save(blogPost);
            return likesCount;
        }

        likesCount = blogPost.getLikeCount() +1;
        likedBy.add(authUser.fullName());
        blogPost.setLikeCount(likedBy.size());
        blogPostRepository.save(blogPost);

        return likesCount;
    }

    public Set<CommentDto> commentBlogPost(CommentDto commentDto, Long id){
        var foundPost = blogPostRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Blog Post not found"));
        var foundComments = foundPost.getComments();

        var comment = mapper.map(commentDto, BlogPostComment.class);

        var authUser = getAuthenticatedUserEntity();
        comment.setUser(authUser);

        comment.setBlogPost(foundPost);
        foundComments.add(comment);
        commentsRepository.save(comment);

        foundPost.setComments(foundComments);
        foundPost.setCommentCount(foundPost.getCommentCount() + 1);
        var saved = blogPostRepository.save(foundPost);

        Set<CommentDto> commentSet = new HashSet<>();

        saved.getComments().forEach(c ->
                commentSet.add(mapper.map(saved.getComments(), CommentDto.class)));

        return commentSet;
    }
}