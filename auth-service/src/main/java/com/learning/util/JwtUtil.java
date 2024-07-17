package com.learning.util;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

	private static final String secretKey = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";
    private static final long expiredMs = 86400000; //24h
	
    public String generateAccessToken(Authentication authentication) {
    	
    	String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    	
    	String role = authorities.split(":,")[0];
    	//String auth = authorities.split(":,")[1];
    	
    	UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    	
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", role);
        //claims.put("aut", auth);
        claims.put("sub", userDetails.getUsername());
        //claims.put("pwd", userDetails.getPassword());
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    public boolean isExpiredToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
        return claims.getExpiration().after(new Date(System.currentTimeMillis()));
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
        return claims.get("username", String.class);
    }
    
    private Key getSignKey() {	
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
        
    }
	    
}