package com.athiban.task_management.service;

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
        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            throw new EmailAlreadyExistsException("Email already registered");
        }

        User user=new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.MEMBER);

        userRepository.save(user);
    }

    public LoginResponse login(LoginRequest request){
        User user=userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new AuthenticationException("Invalid email or password"));

        if(!passwordEncoder.matches(request.getPassword(),user.getPassword())){
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
        return new LoginResponse(accessToken,refreshTokenValue);
    }

    @Transactional
    public LoginResponse refresh(RefreshTokenRequest request){
        RefreshToken refreshToken=refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(()->new AuthenticationException("Invalid refresh token"));

        if(refreshToken.isRevoked()){
            refreshTokenRepository.deleteByUser(refreshToken.getUser());
            throw new AuthenticationException("Refresh token has been revoked");
        }

        if(refreshToken.getExpiresAt().isBefore(LocalDateTime.now())){
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
        return new LoginResponse(newAccessToken,newRefreshTokenValue);
    }

    public void logout(RefreshTokenRequest request){
        RefreshToken refreshToken=refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(()->new AuthenticationException("Invalid refresh token"));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }
}
