package com.interviewlab.service.implementation;

import com.interviewlab.service.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtServiceImp implements JwtService {

    private final SecretKey secretKey;
    private final long expiration;

    public JwtServiceImp(
           @Value("${jwt.secret}") String secret,
           @Value("${jwt.expiration}") long expiration
    )
    {
        this.secretKey= Keys.hmacShaKeyFor(
                java.util.Base64
                        .getDecoder()
                        .decode(secret)
        );
        this.expiration=expiration;
    }

    @Override
    public String generateToken(String username) {

        Date now = new Date();
        return Jwts
                .builder()
                .subject(username)
                .issuedAt(now)
                .expiration(
                        new Date(now.getTime() + expiration)
                )
                .signWith(secretKey)
                .compact();
    }
}
