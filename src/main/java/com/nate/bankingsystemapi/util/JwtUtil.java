package com.nate.bankingsystemapi.util;

import com.nate.bankingsystemapi.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.util.Date;

public class JwtUtil {

    private static final String SECRET_KEY = "my-long-secret-key-should-be-long-for-login";
    private static final long EXPIRE_TIME = 1000*60*60*24;


    public static String generateToken(String username, Role role){
        return Jwts.builder()
                .setSubject(username)
                .claim("Role",role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    public static boolean tokenValidation(String token){
        try{
            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                    .build()
                    .parseClaimsJws(token);

            return true;
        }
        catch (Exception e){
            return false;
        }
    }


    public static String extractUsername(String token){
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }
}
