package com.example.reading.service;

import com.example.reading.domain.User;
import com.example.reading.dto.ApiDtos.*;
import com.example.reading.exception.ApiException;
import com.example.reading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Transactional
    public UserResponse signup(SignupRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "이미 사용 중인 이메일입니다."
            );
        }

        User user = new User(
                email,
                passwordEncoder.encode(request.password()),
                request.nickname().strip()
        );

        return UserResponse.from(userRepository.save(user));
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository
                .findByEmail(normalizeEmail(request.email()))
                .orElseThrow(this::loginFailed);

        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )) {
            throw loginFailed();
        }

        return tokenService.issue(user.getId());
    }

    private String normalizeEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    private ApiException loginFailed() {
        return new ApiException(
                HttpStatus.UNAUTHORIZED,
                "이메일 또는 비밀번호가 올바르지 않습니다."
        );
    }
}