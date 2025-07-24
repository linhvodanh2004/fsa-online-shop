package fsa.project.online_shop.services.implement;

import fsa.project.online_shop.models.User;
import fsa.project.online_shop.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String loginIdentifier) throws UsernameNotFoundException {
        User user = findUserByIdentifier(loginIdentifier);

        if (user == null) {
            throw new UsernameNotFoundException("User not found with identifier: " + loginIdentifier);
        }

        if (!user.getStatus()) {
            throw new UsernameNotFoundException("User account is disabled");
        }

        // Only allow password login for local accounts
        if ("GOOGLE".equals(user.getProvider())) {
            throw new UsernameNotFoundException("Google accounts must use OAuth2 login");
        }

        List<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().getName())
        );

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail()) // Use email as primary identifier
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.getStatus())
                .build();
    }

    /**
     * Find user by email, phone, or username
     */
    private User findUserByIdentifier(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return null;
        }

        String trimmedIdentifier = identifier.trim();

        // Try to find by email first (most common)
        User user = userService.findByEmail(trimmedIdentifier);
        if (user != null) {
            log.debug("User found by email: {}", trimmedIdentifier);
            return user;
        }

        // Try to find by username
        user = userService.findByUsername(trimmedIdentifier);
        if (user != null) {
            log.debug("User found by username: {}", trimmedIdentifier);
            return user;
        }

        // Try to find by phone number
        user = userService.findByPhone(trimmedIdentifier);
        if (user != null) {
            log.debug("User found by phone: {}", trimmedIdentifier);
            return user;
        }

        log.debug("User not found with identifier: {}", trimmedIdentifier);
        return null;
    }
}
