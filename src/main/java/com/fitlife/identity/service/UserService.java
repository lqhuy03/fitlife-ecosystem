package com.fitlife.identity.service;

import com.fitlife.identity.dto.UserCreationRequest;
import com.fitlife.identity.dto.UserResponse;

public interface UserService {
    UserResponse createUser(UserCreationRequest request);
}
