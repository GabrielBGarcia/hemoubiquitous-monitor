package com.ufg.hemoubiquitous_monitor.controller;

import com.ufg.hemoubiquitous_monitor.config.security.TokenService;
import com.ufg.hemoubiquitous_monitor.exception.UserAlreadyExistsException;
import com.ufg.hemoubiquitous_monitor.model.LoginDto;
import com.ufg.hemoubiquitous_monitor.model.LoginResponseDto;
import com.ufg.hemoubiquitous_monitor.model.RegisterDto;
import com.ufg.hemoubiquitous_monitor.model.User;
import com.ufg.hemoubiquitous_monitor.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
    import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
public class AuthController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @PostMapping("/login")
    @Operation(description = "login do gestor de saúde")
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginDto loginData) throws Exception {
        var usernamePassword = new UsernamePasswordAuthenticationToken(loginData.username(), loginData.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var token = this.tokenService.generateToken((User)auth.getPrincipal());
        System.out.println(token);


        return ResponseEntity.status(HttpStatus.OK).body(new LoginResponseDto(token));
    }

    @PostMapping("/register")
    @Operation(description = "Cadastro do gestor de saúde")
    public ResponseEntity<User> register(@RequestBody @Valid RegisterDto registerData) throws UserAlreadyExistsException {
        if(this.userRepository.findByUsername(registerData.username()) != null){
            throw new UserAlreadyExistsException("Usuário já existe");
        }

            var password = new BCryptPasswordEncoder().encode(registerData.password());
            var user = new User(registerData.nome(), registerData.username(), password, registerData.uf());

            this.userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
