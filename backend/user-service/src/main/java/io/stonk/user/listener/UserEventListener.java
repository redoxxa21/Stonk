package io.stonk.user.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.stonk.user.dto.UserRegisteredEvent;
import io.stonk.user.entity.User;
import io.stonk.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user-registration", groupId = "user-service-group")
    public void handleUserRegisteredEvent(String eventJson) {
        log.info("Received UserRegisteredEvent: {}", eventJson);
        try {
            UserRegisteredEvent event = objectMapper.readValue(eventJson, UserRegisteredEvent.class);
            log.info("Received UserRegisteredEvent for username: {}", event.getUsername());

            if (userRepository.existsByUsername(event.getUsername())) {
                log.warn("User {} already exists in user-service, skipping", event.getUsername());
                return;
            }

            User user = User.builder()
                    .id(event.getId()) // Optional if ID generation strategy is IDENTITY, but ensures sync
                    .username(event.getUsername())
                    .email(event.getEmail())
                    .role(event.getRole())
                    .build();

            userRepository.save(user);
            log.info("Successfully created user profile for {}", event.getUsername());
        } catch (Exception e) {
            log.error("Failed to process UserRegisteredEvent", e);
        }
    }
}
