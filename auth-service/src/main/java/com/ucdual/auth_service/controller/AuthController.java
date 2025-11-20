package com.ucdual.auth_service.controller;

import com.ucdual.auth_service.dto.LoginRequest;
import com.ucdual.auth_service.dto.RegisterRequest;
import com.ucdual.auth_service.model.User;
import com.ucdual.auth_service.service.UserService;
import com.ucdual.auth_service.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

   @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String token = userService.login(request);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer ", "");
        boolean valid = jwtUtil.validateToken(token);
        return ResponseEntity.ok(Map.of("valid", valid));
    }
}
