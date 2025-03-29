package com.job.interview.blog.controller;

import com.job.interview.blog.service.impl.BlogPostProcessor;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@Log4j2
@Tag(name = "Blog post")
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "blog")
public class BlogPostController {

    private final BlogPostProcessor blogPostService;

    @PostMapping
    public ResponseEntity<String> uploadAndPublishBlogPost(
            @RequestPart("blogPost") String blogPostJson,
            @RequestPart("htmlContent") MultipartFile htmlFile) throws IOException {
        blogPostService.saveAndPublish(blogPostJson, htmlFile);
        return ResponseEntity.ok("BlogPost uploaded successfully!");
    }
}



//publishBlogPost(blogPost: BlogPost): Observable<BlogPost> {
//    return this.http.post<BlogPost>(this.apiUrl, blogPost);
//}
//
//getAllShortBlogPosts(page: number, size: number): Observable<Page<BlogPost>> { //TODO this will get short version of posts
//        return this.http.get<Page<BlogPost>>(`${this.apiUrl}?page=${page}&size=${size}`);
//        }
//
//getBlogPost(postId: number, userId: number): Observable<BlogPost> { //TODO this will increment view count, extended version of post
//    return this.http.get<BlogPost>(`${this.apiUrl}/${postId}?userId=${userId}`);
//}
//
//likeUnlikePost(likeRequest: LikeRequest): Observable<void> {
//        return this.http.post<void>(`${this.apiUrl}/like`, likeRequest);
//        }