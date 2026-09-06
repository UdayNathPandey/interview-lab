package com.interviewlab.repository.mysql;

import com.interviewlab.entity.mysql.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser,Long> {

    Optional<AppUser> findByUsername(String userName);
}
