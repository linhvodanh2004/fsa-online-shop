package fsa.project.online_shop.configs;

import fsa.project.online_shop.models.Role;
import fsa.project.online_shop.models.constant.UserRole;
import fsa.project.online_shop.repositories.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
public class DataInitializer {
    private final RoleRepository roleRepository;
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(DataInitializer.class);
    @Bean
    public CommandLineRunner initRoles() {
        return args -> {
            createRoleIfNotExists(UserRole.ADMIN);
            createRoleIfNotExists(UserRole.USER);
        };
    }

    private void createRoleIfNotExists(String roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
            logger.info("Created role: " + roleName);
        }
    }
}
