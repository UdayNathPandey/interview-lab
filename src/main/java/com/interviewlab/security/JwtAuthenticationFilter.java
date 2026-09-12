package com.interviewlab.security;

import com.interviewlab.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            FilterChain filterChain
    )
            throws ServletException, IOException
    {
        // get the header
        String header = httpServletRequest.getHeader("Authorization");
        if(header == null || !header.startsWith("Bearer "))
        {
            filterChain.doFilter(httpServletRequest,httpServletResponse);
            return;
        }

        // get the tokn
        String token = header.substring(7);

        String username;

        try
        {
            username= jwtService.extractUsername(token);
        }
        catch(Exception ex)
        {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

        // set new authentication if below if succeeds
        if(username!=null &&
                SecurityContextHolder.getContext()
                        .getAuthentication()==null
        )
        {
            // create the userDetails from database user
            UserDetails userDetails = customUserDetailsService
                    .loadUserByUsername(username);

            // proceed only if token is valid
            if(jwtService.isTokenValid(token))
            {
                // create the authentication
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                //save the authentication in empty security context
                SecurityContextHolder
                        .getContext()
                        .setAuthentication(usernamePasswordAuthenticationToken);
            }

        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
