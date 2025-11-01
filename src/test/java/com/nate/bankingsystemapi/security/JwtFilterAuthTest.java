package com.nate.bankingsystemapi.security;

import com.nate.bankingsystemapi.model.CustomerDetails;
import com.nate.bankingsystemapi.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtFilterAuthTest {
    @Mock
    private UserDetailsService details;
    @InjectMocks
    private JwtFilterAuth auth;
    @Mock
    private FilterChain chain;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private StringWriter stringWriter;
    private PrintWriter printWriter;


    @BeforeEach
    void startUp(){
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);

        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldPassThroughAuthEndPoints() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/auth/login");
        auth.doFilterInternal(request,response,chain);
        verify(chain,times(1)).doFilter(request,response);
    }

    @Test
    void shouldReturn401WhenMissingAuthorization() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/account/1");
        when(request.getHeader("Authorization")).thenReturn(null);
        when(response.getWriter()).thenReturn(printWriter);

        auth.doFilterInternal(request,response,chain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(chain,never()).doFilter(request,response);
    }


    @Test
    void shouldAuthenticateWhenTokenIsValid() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/account/1");
        when(request.getHeader("Authorization")).thenReturn("Bearer Valid-Token");


        CustomerDetails customerDetails = mock(CustomerDetails.class);
        when(customerDetails.getAuthorities()).thenReturn(null);

        when(details.loadUserByUsername("test")).thenReturn(customerDetails);

        try (MockedStatic<JwtUtil> jwtMock = Mockito.mockStatic(JwtUtil.class)){
            jwtMock.when(()-> JwtUtil.tokenValidation("Valid-Token")).thenReturn(true);
            jwtMock.when(()->JwtUtil.extractUsername("Valid-Token")).thenReturn("test");

            auth.doFilterInternal(request,response,chain);
        }

        verify(chain,times(1)).doFilter(request,response);
    }


}
