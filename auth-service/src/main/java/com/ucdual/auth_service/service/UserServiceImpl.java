package com.ucdual.auth_service.service;

import com.ucdual.auth_service.dto.LoginRequest;
import com.ucdual.auth_service.dto.LoginResponse;
import com.ucdual.auth_service.dto.RegisterRequest;
import com.ucdual.auth_service.model.User;
import com.ucdual.auth_service.model.Account;
import com.ucdual.auth_service.repository.UserRepository;
import com.ucdual.auth_service.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.ucdual.auth_service.util.JwtUtil;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository; // Adicionado
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public User register(RegisterRequest request) {
        // Cria usuário
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user = userRepository.save(user);

        // Cria conta com saldo inicial 0
        Account account = new Account();
        account.setUserId(user.getId());
        account.setBalance(0.0);
        accountRepository.save(account);

        return user;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getId());
        return new LoginResponse(token, user.getId().toString());
    }
    
    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
