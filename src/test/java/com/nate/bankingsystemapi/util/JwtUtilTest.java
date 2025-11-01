package com.nate.bankingsystemapi.util;

import com.nate.bankingsystemapi.model.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private String username = "tester";
    private Role role = Role.USER;

    @Test
    void testGenerateToken(){
        String token = JwtUtil.generateToken(username,role);

        assertNotNull(token,"should not be null");
        assertTrue(JwtUtil.tokenValidation(token));
    }

    @Test
    void testValidateToken_Fail(){
        String token = "Fake-Token-Test";

        assertNotNull(token);
        assertFalse(JwtUtil.tokenValidation(token),"should be false because not a valid token");
    }

    @Test
    void testExtractUsername(){
        String token = JwtUtil.generateToken(username,role);

        assertNotNull(token);

        String extractedUsername = JwtUtil.extractUsername(token);

        assertEquals(extractedUsername,username,"extracted username should be the same as the original");
    }
}
