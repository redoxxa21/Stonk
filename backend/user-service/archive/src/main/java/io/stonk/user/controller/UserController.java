package io.stonk.user.controller;

import io.stonk.user.entity.UserProfile;
import io.stonk.user.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /user/me
    @GetMapping("/me")
    public UserProfile getMyProfile(Authentication authentication) {

        String username = authentication.getName();

        return userService.getUserByUsername(username);
    }

    // POST /user/me
    @PostMapping("/me")
    public UserProfile createProfile(Authentication authentication) {

        String username = authentication.getName();

        return userService.createUserProfile(username);
    }
}