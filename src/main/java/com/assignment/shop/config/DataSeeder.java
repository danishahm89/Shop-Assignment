package com.assignment.shop.config;

import com.assignment.shop.users.entity.User;
import com.assignment.shop.users.enums.Role;
import com.assignment.shop.users.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (userRepo.count() == 0) {
            log.info("Seeding test users...");

            userRepo.save(User.builder()
                    .username("admin")
                    .email("admin@shop.com")
                    .password(encoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build());

            userRepo.save(User.builder()
                    .username("premium")
                    .email("premium@shop.com")
                    .password(encoder.encode("premium123"))
                    .role(Role.PREMIUM_USER)
                    .build());

            userRepo.save(User.builder()
                    .username("user")
                    .email("user@shop.com")
                    .password(encoder.encode("user123"))
                    .role(Role.USER)
                    .build());

            log.info("Test users seeded");
        }
    }
}


