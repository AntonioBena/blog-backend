package com.job.interview.blog.service.impl;

import com.job.interview.blog.repository.UserRepository;
import com.job.interview.blog.repository.WriterRequestRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import static com.job.interview.blog.model.user.UserRole.WRITER;

@Log4j2
@Service
@RequiredArgsConstructor
public class WriterCreationService {

    private final UserRepository userRepository;
    private final WriterRequestRepository writerRequestRepository;

    public void signUpAsWriter(Long userId){
        var foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Requested user not present!"));
        //TODO add registering new writer
        // writer should request writer role and blog admin will alter requested.
        // admin can reject or accept writer request
    }

    @Transactional
    public void acceptWriterRequest(Long userId){
        var foundRequest = writerRequestRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Request for user not present!"));

        var foundUser = foundRequest.getUser();

        foundUser.setRole(WRITER);
        userRepository.save(foundUser);

        writerRequestRepository.delete(foundRequest);
        log.info("New writer created: {}", foundUser);
    }
}