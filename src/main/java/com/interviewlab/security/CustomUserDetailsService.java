package com.interviewlab.security;

import com.interviewlab.entity.mysql.AppUser;
import com.interviewlab.repository.mysql.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException
    {
        System.out.println("🔥 loadUserByUsername CALLED");
        AppUser appUser= appUserRepository.findByUsername(userName)
                .orElseThrow(
                        () -> new UsernameNotFoundException("User not found with name " + userName)
                );

        return new CustomUserDetails(appUser);
    }
}
