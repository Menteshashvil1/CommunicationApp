package com.example.communications.user.controller;

import com.example.communications.user.dto.UserResponse;
import com.example.communications.common.security.UserPrincipal;
import com.example.communications.user.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UserPrincipal currentUser) {
        return userService.getCurrentUser(currentUser.id());
    }
}