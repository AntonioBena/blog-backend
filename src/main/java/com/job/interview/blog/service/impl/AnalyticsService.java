package com.job.interview.blog.service.impl;

import com.job.interview.blog.repository.BlogPostRepository;
import com.job.interview.blog.service.impl.auth.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class AnalyticsService {
    private final BlogPostRepository blogPostRepository;
    private final AuthenticationContext authContext;

    public Map<Integer, Long> countPostsByYear(int year){
        var authUser = authContext.getAuthenticatedUserEntity();
        Collection<Object[]> results = blogPostRepository.countPostsPerMonth(year, authUser.getId());
        return results.stream()
                .collect(Collectors.toMap(r -> (Integer) r[0], r -> (Long) r[1]));
    }
}