package com.ufg.hemoubiquitous_monitor.controller;

import com.ufg.hemoubiquitous_monitor.exception.UserAlreadyExistsException;
import com.ufg.hemoubiquitous_monitor.model.LoginDto;
import com.ufg.hemoubiquitous_monitor.model.LoginResponseDto;
import com.ufg.hemoubiquitous_monitor.model.RegisterDto;
import com.ufg.hemoubiquitous_monitor.model.User;
import com.ufg.hemoubiquitous_monitor.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Operation(description = "login do gestor de saúde")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginDto loginData) throws Exception {
        var response = this.authService.authenticate(loginData);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/register")
    @Operation(description = "Cadastro do gestor de saúde")
    public ResponseEntity<User> register(@RequestBody @Valid RegisterDto registerData) throws UserAlreadyExistsException {
        var user = this.authService.register(registerData);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
