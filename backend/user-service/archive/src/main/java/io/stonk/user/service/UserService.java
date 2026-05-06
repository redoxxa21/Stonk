package io.stonk.user.service;

import io.stonk.user.entity.UserProfile;
import io.stonk.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserProfile getUserByUsername(String username) {

        return userRepository.findByUsername(username)
                .orElseGet(() -> {
                    // AUTO CREATE if not found
                    UserProfile profile = new UserProfile();
                    profile.setUsername(username);
                    profile.setBio("");
                    return userRepository.save(profile);
                });
    }

    public UserProfile createUserProfile(String username) {

        return userRepository.findByUsername(username)
                .orElseGet(() -> {
                    UserProfile profile = new UserProfile();
                    profile.setUsername(username);
                    profile.setBio("");
                    return userRepository.save(profile);
                });
    }
}