package fsa.project.online_shop.repositories;

import fsa.project.online_shop.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByUsername(String username);

    boolean existsByPhone(String phone);

    // Find by role
    @Query("SELECT u FROM User u WHERE u.role.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);

    // Find by provider
    List<User> findByProvider(String provider);

    // Find by status
    List<User> findByStatus(Boolean status);

    // Count methods
    long countByStatus(Boolean status);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role.name = :roleName")
    long countByRoleName(@Param("roleName") String roleName);

    @Query("SELECT COUNT(u) FROM User u WHERE u.provider = :provider")
    long countByProvider(@Param("provider") String provider);

    // Search methods
    @Query("SELECT u FROM User u WHERE " +
            "(:query IS NULL OR " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.fullname) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.phone) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "(:roleName IS NULL OR u.role.name = :roleName) AND " +
            "(:provider IS NULL OR u.provider = :provider) AND " +
            "(:status IS NULL OR u.status = :status)")
    List<User> findUsersWithFilters(@Param("query") String query,
                                    @Param("roleName") String roleName,
                                    @Param("provider") String provider,
                                    @Param("status") Boolean status);

    // Find active users with orders (for analytics)
    @Query("SELECT DISTINCT u FROM User u JOIN u.orders o WHERE u.status = true")
    List<User> findActiveUsersWithOrders();

    // Find users by email domain
    @Query("SELECT u FROM User u WHERE u.email LIKE CONCAT('%@', :domain)")
    List<User> findByEmailDomain(@Param("domain") String domain);

    // Find recently created users
    @Query("SELECT u FROM User u WHERE u.id IN (SELECT MAX(u2.id) FROM User u2 GROUP BY u2.id) ORDER BY u.id DESC")
    List<User> findRecentUsers(org.springframework.data.domain.Pageable pageable);
}
