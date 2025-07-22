package fsa.project.online_shop.services.implement;

import fsa.project.online_shop.models.User;
import fsa.project.online_shop.repositories.UserRepository;
import fsa.project.online_shop.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ApplicationContext applicationContext;

    @Override
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        try {
            return userRepository.findAll(pageable);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching users with pagination: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        try {
            return userRepository.findById(id).orElse(null);
        } catch (Exception e) {
            throw new RuntimeException("Error finding user by ID: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        try {
            return userRepository.findByEmail(email).orElse(null);
        } catch (Exception e) {
            throw new RuntimeException("Error finding user by email: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        try {
            return userRepository.findByUsername(username).orElse(null);
        } catch (Exception e) {
            throw new RuntimeException("Error finding user by username: " + e.getMessage(), e);
        }
    }

    @Override
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        try {
            // Validate required fields
            validateUser(user);

            // Check for duplicate email (excluding current user if updating)
            User existingUserWithEmail = findByEmail(user.getEmail());
            if (existingUserWithEmail != null && !existingUserWithEmail.getId().equals(user.getId())) {
                throw new RuntimeException("Email already exists: " + user.getEmail());
            }

            // Check for duplicate username (excluding current user if updating)
            User existingUserWithUsername = findByUsername(user.getUsername());
            if (existingUserWithUsername != null && !existingUserWithUsername.getId().equals(user.getId())) {
                throw new RuntimeException("Username already exists: " + user.getUsername());
            }

            // Encode password if it's a new user or password is being updated
            if (user.getId() == null || StringUtils.hasText(user.getPassword())) {
                PasswordEncoder encoder = applicationContext.getBean(PasswordEncoder.class);
                user.setPassword(encoder.encode(user.getPassword()));
            }

            // Set default status if null
            if (user.getStatus() == null) {
                user.setStatus(true);
            }

            return userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Error saving user: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }

        try {
            User user = findById(id);
            if (user == null) {
                throw new RuntimeException("User not found with ID: " + id);
            }

            // Prevent deletion of admin users
            if (user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getName())) {
                throw new RuntimeException("Cannot delete admin users");
            }

            userRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Error deleting user: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return false;
        }

        try {
            return userRepository.existsByEmail(email);
        } catch (Exception e) {
            throw new RuntimeException("Error checking email existence: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return false;
        }

        try {
            return userRepository.existsByUsername(username);
        } catch (Exception e) {
            throw new RuntimeException("Error checking username existence: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> searchUsers(String query, String role, String provider, Boolean status) {
        try {
            // Use paginated approach to avoid loading all users into memory
            Page<User> userPage = userRepository.findAll(Pageable.unpaged());
            List<User> users = userPage.getContent();

            return users.stream()
                    .filter(user -> matchesQuery(user, query))
                    .filter(user -> matchesRole(user, role))
                    .filter(user -> matchesProvider(user, provider))
                    .filter(user -> matchesStatus(user, status))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Error searching users: " + e.getMessage(), e);
        }
    }

    @Override
    public User updateStatus(Long userId, Boolean status) {
        if (userId == null || status == null) {
            throw new IllegalArgumentException("User ID and status cannot be null");
        }

        try {
            User user = findById(userId);
            if (user == null) {
                throw new RuntimeException("User not found with ID: " + userId);
            }

            user.setStatus(status);
            return save(user);
        } catch (Exception e) {
            throw new RuntimeException("Error updating user status: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalUserCount() {
        try {
            return userRepository.count();
        } catch (Exception e) {
            throw new RuntimeException("Error counting users: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long getActiveUserCount() {
        try {
            return userRepository.countByStatus(true);
        } catch (Exception e) {
            throw new RuntimeException("Error counting active users: " + e.getMessage(), e);
        }
    }

    // Private helper methods
    private void validateUser(User user) {
        if (!StringUtils.hasText(user.getUsername())) {
            throw new IllegalArgumentException("Username is required");
        }

        if (!StringUtils.hasText(user.getEmail())) {
            throw new IllegalArgumentException("Email is required");
        }

        if (user.getId() == null && !StringUtils.hasText(user.getPassword())) {
            throw new IllegalArgumentException("Password is required for new users");
        }

        // Validate email format
        if (!isValidEmail(user.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$");
    }

    private boolean matchesQuery(User user, String query) {
        if (!StringUtils.hasText(query)) {
            return true;
        }

        String searchQuery = query.toLowerCase();
        return (user.getUsername() != null && user.getUsername().toLowerCase().contains(searchQuery)) ||
                (user.getFullname() != null && user.getFullname().toLowerCase().contains(searchQuery)) ||
                (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchQuery)) ||
                (user.getPhone() != null && user.getPhone().toLowerCase().contains(searchQuery));
    }

    private boolean matchesRole(User user, String role) {
        if (!StringUtils.hasText(role)) {
            return true;
        }

        return user.getRole() != null && role.equalsIgnoreCase(user.getRole().getName());
    }

    private boolean matchesProvider(User user, String provider) {
        if (!StringUtils.hasText(provider)) {
            return true;
        }

        return provider.equalsIgnoreCase(user.getProvider());
    }

    private boolean matchesStatus(User user, Boolean status) {
        if (status == null) {
            return true;
        }

        return status.equals(user.getStatus());
    }
}