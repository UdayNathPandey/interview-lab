package com.interviewlab.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthTestController {

    private final AuthenticationManager authenticationManager;

    @PostMapping("/test")
    public ResponseEntity<String> testAuthentication(
            @RequestParam String username,
            @RequestParam(name="password") String password
    )
    {

        Authentication authentication = new UsernamePasswordAuthenticationToken(username,password);

        Authentication result = authenticationManager.authenticate(authentication);
        return ResponseEntity.status(HttpStatus.OK).body("Authenticated: "+result);
    }


}
