package com.example.communications.user.service;

import com.example.communications.user.dto.UserResponse;
import com.example.communications.user.model.User;
import com.example.communications.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow();

        return UserResponse.from(user);
    }
}
