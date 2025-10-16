package com.ufg.hemoubiquitous_monitor.config.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.ufg.hemoubiquitous_monitor.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;

@Service
public class TokenService {
    @Value("${api.security.secret.token}")
    private String secret;

    public String generateToken(User user) throws RuntimeException {
        try{
        Algorithm algorithm = Algorithm.HMAC256(this.secret);
        String token = JWT.create()
                .withIssuer("auth-issuer")
                .withSubject(user.getUsername())
                .withExpiresAt(this.getExpirationDate())
                .sign(algorithm);

        return token;
    } catch(JWTCreationException exception){
            throw new RuntimeException("Erro ao gerar token JWT", exception);
        }
}

public String validadeToken(String token){
        try{
        Algorithm algorithm = Algorithm.HMAC256(this.secret);
            return JWT.require(algorithm)
                .withIssuer("auth-issuer")
                .build()
                    .verify(token)
                .getSubject();
        } catch(JWTCreationException exception){
            return "";
        }
}

    private Instant getExpirationDate() {
        return LocalDateTime.now().plusHours(2).toInstant(java.time.ZoneOffset.of("-03:00"));
    }
}
