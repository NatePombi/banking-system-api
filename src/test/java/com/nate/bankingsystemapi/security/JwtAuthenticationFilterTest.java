package com.nate.bankingsystemapi.security;

import com.nate.bankingsystemapi.model.user.entity.TestUser;
import com.nate.bankingsystemapi.model.user.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {
    @Mock
    private UserDetailsService details;
    @InjectMocks
    private JwtAuthenticationFilter auth;
    @Mock
    private FilterChain chain;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private JwtService jwtService;

    private StringWriter stringWriter;
    private PrintWriter printWriter;
    private User testUser;


    @BeforeEach
    void startUp(){
        testUser = new TestUser(12L,"Tester", "tester","password");
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldPassThroughAuthEndPoints() throws ServletException, IOException {
        auth.doFilterInternal(request,response,chain);
        verify(chain,times(1)).doFilter(request,response);
    }

    @Test
    void shouldReturn401WhenMissingAuthorization() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        auth.doFilterInternal(request,response,chain);

        verify(chain).doFilter(request,response);
    }


    @Test
    void shouldAuthenticateWhenTokenIsValid() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer Valid-Token");
        when(jwtService.isTokenValid(eq("Valid-Token"),any())).thenReturn(Boolean.TRUE);
        when(jwtService.extractUsername("Valid-Token")).thenReturn("tester");

        User user = mock(User.class);
        when(user.getAuthorities()).thenReturn(List.of());

        when(details.loadUserByUsername("tester")).thenReturn(user);


         auth.doFilterInternal(request,response,chain);

        verify(chain,times(1)).doFilter(request,response);
    }


}
