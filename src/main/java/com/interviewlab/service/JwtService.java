package com.interviewlab.service;

public interface JwtService {
    String generateToken(String userName);
    boolean isTokenValid(String token);

    String extractUsername(String token);
}
