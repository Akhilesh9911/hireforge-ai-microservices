package com.hireforge.auth_service.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private static final String SECRET = "hireforge-test-secret-key-for-jwt-signing-12345";
    private static final String OTHER_SECRET = "another-different-secret-key-for-tampering-test-99";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = createJwtUtil(SECRET, 3600000L);
    }

    private JwtUtil createJwtUtil(String secret, long expiration) {
        JwtUtil util = new JwtUtil();
        ReflectionTestUtils.setField(util, "secret", secret);
        ReflectionTestUtils.setField(util, "expiration", expiration);
        return util;
    }

    @Test
    void generateToken_ShouldReturnNonEmptyToken() {
        String token = jwtUtil.generateToken("akhilesh@gmail.com", 1L);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void validateToken_ShouldReturnTrue_WhenTokenIsValid() {
        String token = jwtUtil.generateToken("akhilesh@gmail.com", 1L);

        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void extractEmail_ShouldReturnCorrectEmail_FromValidToken() {
        String token = jwtUtil.generateToken("akhilesh@gmail.com", 1L);

        assertEquals("akhilesh@gmail.com", jwtUtil.extractEmail(token));
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsExpired() {
        JwtUtil expiredUtil = createJwtUtil(SECRET, -1000L);
        String expiredToken = expiredUtil.generateToken("akhilesh@gmail.com", 1L);

        assertFalse(jwtUtil.validateToken(expiredToken));
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenSignedWithDifferentSecret() {
        JwtUtil attackerUtil = createJwtUtil(OTHER_SECRET, 3600000L);
        String forgedToken = attackerUtil.generateToken("akhilesh@gmail.com", 1L);

        assertFalse(jwtUtil.validateToken(forgedToken));
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsGarbage() {
        assertFalse(jwtUtil.validateToken("not.a.jwt"));
    }
}