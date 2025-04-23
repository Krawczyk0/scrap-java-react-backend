package com.example.demo.controller;

import com.example.demo.filter.JwtUtil;
import com.example.demo.model.UserModel;
import com.example.demo.repository.AuthRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;





@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(AuthenticationManager authenticationManager, AuthRepository authRepository) {
        this.authenticationManager = authenticationManager;
        this.authRepository = authRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserModel user) {
        String username = user.getUsername();
        Map<String, Object> response = new HashMap<>();

        if (authRepository.findByUsername(username).isPresent()) {
            response.put("message", "Użytkownik o podanej nazwie już istnieje");
            response.put("code", "400");
            response.put("status", "error");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        authRepository.save(user);
        response.put("message", "Konto zostało założone");
        response.put("code","201");
        response.put("status","success");
        return ResponseEntity.ok(response);
    }




    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> user, HttpServletResponse response) {
        String username = user.get("username");
        String password = user.get("password");
        Map<String, Object> resMap = new HashMap<>();

        Optional<UserModel> userData = authRepository.findByUsername(username);
        if (userData.isEmpty() || !passwordEncoder.matches(password, userData.get().getPassword())) {
            resMap.put("message", "Błędny email lub hasło");
            resMap.put("code", "400");
            resMap.put("status", "error");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resMap);
        }

        String token = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(JwtUtil.SECRET_KEY)
                .compact();

        ResponseCookie cookie = ResponseCookie.from("jwt",token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .partitioned(true) // Ustawienie Partitioned
                .maxAge(3600) // 1 godzina
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        resMap.put("message", "Pomyślnie zalogowano");
        resMap.put("code", "200");
        resMap.put("status", "success");
        resMap.put("token", token);

        return ResponseEntity.ok(resMap);
    }


    @GetMapping("/secure")
    public ResponseEntity<String> secureEndpoint(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
        }

        String token = authHeader.substring(7);
        String user;
        try {
            user = Jwts.parserBuilder()
                    .setSigningKey(JwtUtil.SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }

        return ResponseEntity.ok("Welcome " + user + "!");
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Welcome");
    }

}


