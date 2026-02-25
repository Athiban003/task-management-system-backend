package com.athiban.task_management.controller;

import com.athiban.task_management.dto.LoginRequest;
import com.athiban.task_management.dto.LoginResponse;
import com.athiban.task_management.dto.RefreshTokenRequest;
import com.athiban.task_management.dto.RegisterRequest;
import com.athiban.task_management.models.RefreshToken;
import com.athiban.task_management.models.User;
import com.athiban.task_management.repository.RefreshTokenRepository;
import com.athiban.task_management.repository.UserRepository;
import com.athiban.task_management.security.JwtUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private  final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthController(UserRepository userRepository,PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil,
                          RefreshTokenRepository refreshTokenRepository){
        this.userRepository=userRepository;
        this.passwordEncoder=passwordEncoder;
        this.jwtUtil=jwtUtil;
        this.refreshTokenRepository=refreshTokenRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request){

        if(userRepository.findByEmail(request.getEmail()).isPresent()){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already registered");
        }

        User user=new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request){

        User user=userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new RuntimeException("Invalid email or password"));

        if(!passwordEncoder.matches(request.getPassword(),user.getPassword())){
            throw new RuntimeException("Invalid email or password");
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

        return  ResponseEntity.ok(new LoginResponse(accessToken,refreshTokenValue));
    }

    @PostMapping("/refresh")
    @Transactional
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest request){

        RefreshToken refreshToken=refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(()->new RuntimeException("Invalid refresh token"));

        if(refreshToken.isRevoked()){
            refreshTokenRepository.deleteByUser(refreshToken.getUser());
            throw new RuntimeException("Refresh token has been revoked");
        }

        if(refreshToken.getExpiresAt().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Refresh token expired");
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

        return ResponseEntity.ok(new LoginResponse(newAccessToken,newRefreshTokenValue));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@Valid @RequestBody RefreshTokenRequest request){
        RefreshToken refreshToken=refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(()->new RuntimeException("Invalid refresh token"));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return ResponseEntity.ok("Logged out successfully");
    }
}
