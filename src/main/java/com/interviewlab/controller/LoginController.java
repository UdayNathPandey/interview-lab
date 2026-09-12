package com.interviewlab.controller;

import com.interviewlab.dto.LoginRequest;
import com.interviewlab.dto.LoginResponse;
import com.interviewlab.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class LoginController {
        private final AuthenticationManager authenticationManager;
        private final JwtService jwtService;

        @PostMapping("/login")
        public ResponseEntity<LoginResponse> login(
                @RequestBody LoginRequest loginRequest
        )
        {
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            );

            Authentication resultAuthentication = authenticationManager
                    .authenticate( authentication);

            String token = jwtService.generateToken(resultAuthentication.getName());

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(
                            new LoginResponse
                                    (
                            "Login Successfull!!",
                            resultAuthentication.getName(),
                                            token
                                    )
                    );

        }
}
