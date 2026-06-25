package com.athiban.task_management.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.athiban.task_management.dto.LoginRequest;
import com.athiban.task_management.dto.LoginResponse;
import com.athiban.task_management.dto.RefreshTokenRequest;
import com.athiban.task_management.dto.RegisterRequest;
import com.athiban.task_management.exception.AuthenticationException;
import com.athiban.task_management.exception.EmailAlreadyExistsException;
import com.athiban.task_management.exception.TokenExpiredException;
import com.athiban.task_management.models.*;
import com.athiban.task_management.repository.RefreshTokenRepository;
import com.athiban.task_management.repository.UserRepository;
import com.athiban.task_management.security.JwtUtil;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;


@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private  final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public  AuthService(UserRepository userRepository , PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil, RefreshTokenRepository refreshTokenRepository){
        this.userRepository=userRepository;
        this.passwordEncoder=passwordEncoder;
        this.jwtUtil=jwtUtil;
        this.refreshTokenRepository=refreshTokenRepository;
    }

    public User getCurrentUser(){
        Authentication auth= SecurityContextHolder.getContext().getAuthentication();
        if(auth == null || !auth.isAuthenticated()){
            throw new AuthenticationException("No authenticated user found");
        }
        String email=auth.getName();
        return  userRepository.findByEmail(email)
                .orElseThrow(()->new IllegalStateException("Authenticated user not found"));
    }

    public void register(RegisterRequest request){
        logger.info("Registering user {}", request.getEmail());
        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new EmailAlreadyExistsException("Email already registered");
        }

        User user=new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.MEMBER);

        userRepository.save(user);
        logger.info("User registered successfully {}", user.getId());
    }

    public LoginResponse login(LoginRequest request){
        logger.info("Login attempt for {}", request.getEmail());

        User user=userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new AuthenticationException("Invalid email or password"));

        if(!passwordEncoder.matches(request.getPassword(),user.getPassword())){
            logger.warn("Failed login attempt for {}", request.getEmail());

            throw new AuthenticationException("Invalid email or password");
        }

        String accessToken=jwtUtil.generateToken(
                user.getEmail(),
                user.getId(),
                user.getRole()
        );

        String refreshTokenValue= UUID.randomUUID().toString();
        RefreshToken refreshToken=new RefreshToken(
                refreshTokenValue,
                user,
                LocalDateTime.now().plusDays(7)
        );

        refreshTokenRepository.save(refreshToken);
        logger.info("Login successful for user {}", user.getId());
        return new LoginResponse(accessToken,refreshTokenValue);
    }

    @Transactional
    public LoginResponse refresh(RefreshTokenRequest request){
        RefreshToken refreshToken=refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(()->new AuthenticationException("Invalid refresh token"));

        logger.info(
                "Refreshing access token for user {}",
                refreshToken.getUser().getId()
        );

        if(refreshToken.isRevoked()){
            refreshTokenRepository.deleteByUser(refreshToken.getUser());
            logger.warn(
                    "Revoked refresh token used by user {}",
                    refreshToken.getUser().getId()
            );
            throw new AuthenticationException("Refresh token has been revoked");
        }

        if(refreshToken.getExpiresAt().isBefore(LocalDateTime.now())){
            logger.warn(
                    "Expired refresh token used by user {}",
                    refreshToken.getUser().getId()
            );
            throw new TokenExpiredException("Refresh token expired");
        }

        User user=refreshToken.getUser();
        String newAccessToken=jwtUtil.generateToken(user.getEmail(),user.getId(),user.getRole());
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        String newRefreshTokenValue=UUID.randomUUID().toString();
        RefreshToken newRefreshToken=new RefreshToken(
                newRefreshTokenValue,
                user,
                LocalDateTime.now().plusDays(7)
        );
        refreshTokenRepository.save(newRefreshToken);
        logger.info("Refresh token generated for user {}", user.getId());

        return new LoginResponse(newAccessToken,newRefreshTokenValue);
    }

    public void logout(RefreshTokenRequest request){
        logger.info("Logout request received");

        RefreshToken refreshToken=refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(()->new AuthenticationException("Invalid refresh token"));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        logger.info(
                "User {} logged out successfully",
                refreshToken.getUser().getId()
        );
    }
}
