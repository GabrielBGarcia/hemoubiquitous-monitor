package com.ufg.hemoubiquitous_monitor.repository;

import com.ufg.hemoubiquitous_monitor.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserRepository extends JpaRepository<User, Long> {
    UserDetails findByUsername(String username);
}
