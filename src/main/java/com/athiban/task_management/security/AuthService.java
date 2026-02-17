package com.athiban.task_management.security;

import com.athiban.task_management.models.User;
import com.athiban.task_management.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@Service
public class AuthService {
    private final UserRepository userRepository;
    public  AuthService(UserRepository userRepository){
        this.userRepository=userRepository;
    }

    public User getCurrentUser(){
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();

        String userId=auth.getName();
        return  userRepository.findById(Long.parseLong(userId))
                .orElseThrow(()->new IllegalStateException("Authenticated user not found"));
    }
}
