package com.nate.bankingsystemapi.security;

import com.nate.bankingsystemapi.model.TestUser;
import com.nate.bankingsystemapi.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {
    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        //Manually inject values normally provide by value
        ReflectionTestUtils.setField(jwtService,"secretKey",
                "c3VwZXItc2VjcmV0LWtleS1zdXBlci1zZWNyZXQta2V5LXN1cGVyLXNlY3JldC1rZXk=");

        ReflectionTestUtils.setField(jwtService,"expiration",3600000L);

        user = new TestUser(12L,"Test","tester","password");

    }

    @Test
    void shouldGenerateToken() {
        String token = jwtService.generateToken(user);
        assertNotNull(token);
    }

    @Test
    void shouldExtractUsernameFromToken() {
        String token = jwtService.generateToken(user);

        String username = jwtService.extractUsername(token);

        assertEquals(user.getUsername(), username);

    }

    @Test
    void shouldBeValidTokenFromUser(){
        String token = jwtService.generateToken(user);

        boolean isValid = jwtService.isTokenValid(token,user);

        assertTrue(isValid);
    }

    @Test
    void shouldNotBeValidTokenFromDifferentUser(){
        String token = jwtService.generateToken(user);

        User notUser = new TestUser(1L,"Not Test","notTester","password");

        boolean isValid = jwtService.isTokenValid(token,notUser);

        assertFalse(isValid);
    }
}
