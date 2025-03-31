package com.job.interview.blog.controller;

import com.job.interview.blog.service.impl.AnalyticsService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Log4j2
@Tag(name = "Blog post Analytics")
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "blog/analytics")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping("/count-by-year/{year}")
    public Map<Integer, Long> getBlogPostCountsByMonths(@PathVariable int year) {
        return analyticsService.countPostsByYear(year);
    }
}