package com.ufg.hemoubiquitous_monitor.service;

import com.ufg.hemoubiquitous_monitor.config.security.TokenService;
import com.ufg.hemoubiquitous_monitor.exception.UserAlreadyExistsException;
import com.ufg.hemoubiquitous_monitor.model.LoginDto;
import com.ufg.hemoubiquitous_monitor.model.LoginResponseDto;
import com.ufg.hemoubiquitous_monitor.model.RegisterDto;
import com.ufg.hemoubiquitous_monitor.model.User;
import com.ufg.hemoubiquitous_monitor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public LoginResponseDto authenticate(LoginDto loginData) throws Exception {
        var usernamePassword = new UsernamePasswordAuthenticationToken(loginData.username(), loginData.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var token = this.tokenService.generateToken((User) auth.getPrincipal());
        return new LoginResponseDto(token, auth.getAuthorities().toString());
    }

    public User register(RegisterDto registerData) throws UserAlreadyExistsException {
        if (this.userRepository.findByUsername(registerData.username()) != null) {
            throw new UserAlreadyExistsException("Usuário já existe");
        }

        var password = passwordEncoder.encode(registerData.password());
        var user = new User(registerData.nome(), registerData.username(), password, registerData.uf(), registerData.city());

        this.userRepository.save(user);

        return user;
    }
}
