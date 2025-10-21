package com.nate.bankingsystemapi.security;

import com.nate.bankingsystemapi.model.CustomerDetails;
import com.nate.bankingsystemapi.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@AllArgsConstructor
public class JwtFilterAuth  extends OncePerRequestFilter {
    private final UserDetailsService service;
    private static final String[] EXCLUDED_PATH ={
            "/auth/login",
            "/auth/register",
            "/swagger-ui",
            "/swagger-ui/",
            "/swagger-ui.html",
            "/swagger-ui/index.html",
            "/v3/api-docs",
            "/v3/api-docs/",
            "/swagger-resources",
            "/swagger-resources/",
            "/webjars/"
    };


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();
        if(excluded(path)){
            filterChain.doFilter(request,response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if(authHeader == null || !authHeader.startsWith("Bearer ")){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing or invalid Authorization");
            return;
        }

        String token = authHeader.substring(7);

        if(!JwtUtil.tokenValidation(token)){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or Expired token");
        }

        CustomerDetails details = (CustomerDetails) service.loadUserByUsername(JwtUtil.extractUsername(token));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(details, null,details.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request,response);


    }


    private boolean excluded(String path){
        return Arrays.stream(EXCLUDED_PATH)
                .anyMatch(path::startsWith);
    }
}
