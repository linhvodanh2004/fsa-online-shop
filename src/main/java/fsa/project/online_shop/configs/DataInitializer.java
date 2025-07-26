package fsa.project.online_shop.configs;

import fsa.project.online_shop.models.Cart;
import fsa.project.online_shop.models.Role;
import fsa.project.online_shop.models.User;
import fsa.project.online_shop.models.constant.UserRole;
import fsa.project.online_shop.repositories.CartRepository;
import fsa.project.online_shop.repositories.RoleRepository;
import fsa.project.online_shop.repositories.UserRepository;
import fsa.project.online_shop.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class DataInitializer {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartRepository cartRepository;
    @Bean
    public CommandLineRunner initRoles() {
        return args -> {
            createRoleIfNotExists(UserRole.ADMIN);
            createRoleIfNotExists(UserRole.USER);
            createAdminUserIfNotExists();
        };
    }

    private void createRoleIfNotExists(String roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
            log.info("Created role: " + roleName);
        }
    }
    private void createAdminUserIfNotExists() {
        if(!userRepository.existsByUsername("admin123")){
            String password = passwordEncoder.encode("admin123");
            Role role = roleRepository.findByName(UserRole.ADMIN).get();
            User user = new User();
            user.setUsername("admin123");
            user.setPassword(password);
            user.setFullname("Admin");
            user.setEmail("gos.administrator@gmail.com");
            user.setPhone("0000000000");
            user.setStatus(true);
            user.setRole(role);
            userRepository.save(user);
            log.info("Created admin user: " + user.getUsername());
            Cart cart = new Cart();
            cart.setUser(user);
            cartRepository.save(cart);
            log.info("Created cart for admin user: " + user.getUsername());
        }
    }
}
