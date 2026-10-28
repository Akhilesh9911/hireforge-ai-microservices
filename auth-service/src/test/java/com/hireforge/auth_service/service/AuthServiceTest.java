package com.hireforge.auth_service.service;

import com.hireforge.auth_service.dto.LoginRequest;
import com.hireforge.auth_service.dto.LoginResponse;
import com.hireforge.auth_service.dto.RegisterRequest;
import com.hireforge.auth_service.dto.RegisterResponse;
import com.hireforge.auth_service.entity.User;
import com.hireforge.auth_service.repository.UserRepository;
import com.hireforge.auth_service.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_ShouldReturnResponse_WhenEmailNotRegistered() {

        RegisterRequest request = new RegisterRequest();
        request.setName("Akhilesh");
        request.setEmail("akhilesh@gmail.com");
        request.setPassword("password123");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("Akhilesh");
        savedUser.setEmail("akhilesh@gmail.com");
        savedUser.setPassword("encodedPassword");

        when(userRepository.findByEmail("akhilesh@gmail.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123"))
                .thenReturn("encodedPassword");
        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);
        when(jwtUtil.generateToken("akhilesh@gmail.com", 1L))
                .thenReturn("mocked.jwt.token");

        RegisterResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Akhilesh", response.getName());
        assertEquals("akhilesh@gmail.com", response.getEmail());
        assertEquals("mocked.jwt.token", response.getToken());
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyRegistered() {

        RegisterRequest request = new RegisterRequest();
        request.setName("Akhilesh");
        request.setEmail("akhilesh@gmail.com");
        request.setPassword("password123");

        User existingUser = new User();
        existingUser.setEmail("akhilesh@gmail.com");

        when(userRepository.findByEmail("akhilesh@gmail.com"))
                .thenReturn(Optional.of(existingUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(request);
        });

        assertEquals("Email already registered", exception.getMessage());
    }

    @Test
    void login_ShouldReturnResponse_WhenCredentialsAreValid() {

        LoginRequest request = new LoginRequest();
        request.setEmail("akhilesh@gmail.com");
        request.setPassword("password123");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setName("Akhilesh");
        existingUser.setEmail("akhilesh@gmail.com");
        existingUser.setPassword("encodedPassword");

        when(userRepository.findByEmail("akhilesh@gmail.com"))
                .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("password123", "encodedPassword"))
                .thenReturn(true);
        when(jwtUtil.generateToken("akhilesh@gmail.com", 1L))
                .thenReturn("mocked.jwt.token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Akhilesh", response.getName());
        assertEquals("akhilesh@gmail.com", response.getEmail());
        assertEquals("mocked.jwt.token", response.getToken());
    }

    @Test
    void login_ShouldThrowException_WhenEmailNotFound() {

        LoginRequest request = new LoginRequest();
        request.setEmail("notexist@gmail.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("notexist@gmail.com"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIsWrong() {

        LoginRequest request = new LoginRequest();
        request.setEmail("akhilesh@gmail.com");
        request.setPassword("wrongPassword");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("akhilesh@gmail.com");
        existingUser.setPassword("encodedPassword");

        when(userRepository.findByEmail("akhilesh@gmail.com"))
                .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword"))
                .thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });

        assertEquals("Invalid email or password", exception.getMessage());
    }
}