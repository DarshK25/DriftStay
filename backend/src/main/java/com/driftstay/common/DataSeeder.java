package com.driftstay.common;

import com.driftstay.user.entity.Role;
import com.driftstay.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            Role userRole = new Role();
            userRole.setName("USER");
            userRole.setDescription("Standard authenticated user");
            roleRepository.save(userRole);
            log.info("Seeded default role: USER");
        }
    }
}
