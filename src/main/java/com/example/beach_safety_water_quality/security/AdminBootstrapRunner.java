package com.example.beach_safety_water_quality.security;

import com.example.beach_safety_water_quality.model.Role;
import com.example.beach_safety_water_quality.model.User;
import com.example.beach_safety_water_quality.repo.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrapRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.bootstrap.enabled:false}")
    private boolean enabled;

    @Value("${app.admin.bootstrap.username:admin}")
    private String adminUsername;

    @Value("${app.admin.bootstrap.password:CHANGE_ME}")
    private String adminPassword;

    public AdminBootstrapRunner(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!enabled) {
            logger.info("Admin bootstrap is disabled.");
            return;
        }

        if (userRepo.existsByUsername(adminUsername)) {
            logger.info("Admin bootstrap user '{}' already exists. Skipping creation.", adminUsername);
            return;
        }

        User adminUser = new User();
        adminUser.setUsername(adminUsername);
        adminUser.setPassword(passwordEncoder.encode(adminPassword));
        adminUser.setRole(Role.ADMIN);
        adminUser.setEnabled(true);

        userRepo.save(adminUser);
        logger.info("Admin bootstrap user '{}' created successfully with ADMIN role.", adminUsername);
    }
}