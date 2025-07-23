package fsa.project.online_shop.services;

import fsa.project.online_shop.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    // Basic CRUD operations
    Page<User> getAllUsers(Pageable pageable);
    User findById(Long id);
    User findByEmail(String email);
    User findByUsername(String username);
    User findByPhone(String phone);
    User save(User user);
    void deleteById(Long id);

    // Existence checks
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    // Search and filter operations
    List<User> searchUsers(String query, String role, String provider, Boolean status);

    // Status management
    User updateStatus(Long userId, Boolean status);

    // Statistics
    long getTotalUserCount();
    long getActiveUserCount();

    public boolean checkEmailExists(String email);
    public User getUserByEmail(String email);
    public User getUserById(Long id);
    public User handleSaveUser(User user);
}