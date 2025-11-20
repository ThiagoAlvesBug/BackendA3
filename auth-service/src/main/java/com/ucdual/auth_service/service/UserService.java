package com.ucdual.auth_service.service;

import com.ucdual.auth_service.dto.LoginRequest;
import com.ucdual.auth_service.dto.RegisterRequest;
import com.ucdual.auth_service.model.User;

public interface UserService {
    User register(RegisterRequest request);
    String login(LoginRequest request);
}
