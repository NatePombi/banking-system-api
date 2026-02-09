package com.nate.bankingsystemapi.security;

import com.nate.bankingsystemapi.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {
    @Value("${security.jwt.secret}")
    private String secretKey;
    @Value("${security.jwt.expiration}")
    private long expiration;


    public String generateToken(User user){
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("Role",user.getRole())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, User user){
        String username = extractUsername(token);
        return user.getUsername().equals(username) && !isTokenExpired(token);
    }


    public String extractUsername(String token){
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    private boolean isTokenExpired(String token){
        return extractClaims(token).getExpiration().before(new Date());
    }

    private Claims extractClaims(String token){
       return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey(){
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

}
