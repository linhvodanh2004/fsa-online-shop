package fsa.project.online_shop.services.implement;

import fsa.project.online_shop.models.Role;
import fsa.project.online_shop.models.User;
import fsa.project.online_shop.models.constant.UserRole;
import fsa.project.online_shop.services.RoleService;
import fsa.project.online_shop.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserService userService;
    private final RoleService roleService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {
        try {
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            log.info("🔐 OAuth2 Login attempt with provider: {}", registrationId);

            OAuth2User oAuth2User = super.loadUser(userRequest);
            Map<String, Object> attributes = oAuth2User.getAttributes();
            log.info("📋 OAuth2 User attributes: {}", attributes.keySet());

            // Google returns 'id' instead of 'sub'
            String googleId = (String) attributes.get("id");
            String sub = googleId; // Use Google ID as sub
            String email = (String) attributes.get("email");
            String fullname = (String) attributes.get("name");
            String picture = (String) attributes.get("picture");
            String givenName = (String) attributes.get("given_name");
            String familyName = (String) attributes.get("family_name");
            Boolean emailVerified = (Boolean) attributes.get("email_verified");

            log.info("👤 OAuth2 User info - Email: {}, Name: {}, GoogleID: {}, Picture: {}, Verified: {}",
                    email, fullname, googleId, picture, emailVerified);

            // Validate required fields
            if (email == null || email.trim().isEmpty()) {
                log.error("❌ Email is null or empty from OAuth2 provider");
                OAuth2Error error = new OAuth2Error("invalid_user_info", "Email not provided by OAuth2 provider", null);
                throw new OAuth2AuthenticationException(error);
            }

            if (googleId == null || googleId.trim().isEmpty()) {
                log.error("❌ Google ID is null or empty from OAuth2 provider");
                OAuth2Error error = new OAuth2Error("invalid_user_info", "Google ID not provided by OAuth2 provider", null);
                throw new OAuth2AuthenticationException(error);
            }

            // Set role admin for some specific account
            Role userRole = roleService.getRoleByName(UserRole.USER);
            if (userRole == null) {
                log.error("❌ USER role not found in database");
                OAuth2Error error = new OAuth2Error("role_not_found", "USER role not found in database", null);
                throw new OAuth2AuthenticationException(error);
            }

            User user = userService.findByEmail(email);
            // If user is null, add this oauth user to DB
            if (user == null) {
                log.info("🆕 Creating new OAuth2 user with email: {}", email);
                User newUser = User.builder()
                        .role(userRole)
                        .status(true)
                        .fullname(fullname != null ? fullname : "Google User")
                        .email(email)
                        .username("google_user_" + googleId)
                        .password(null) // OAuth2 users don't need password
                        .provider(registrationId.toUpperCase())
                        .build();
                user = userService.handleSaveUser(newUser);
                log.info("✅ New OAuth2 user created with ID: {}", user.getId());
            } else {
                log.info("👤 Existing user found with email: {}", email);
                // if user is not null, check it's enabled or not
                if (!user.getStatus()) {
                    log.warn("🚫 User account is locked: {}", email);
                    OAuth2Error error = new OAuth2Error("account_locked", "User account is locked", null);
                    throw new OAuth2AuthenticationException(error);
                }
                userRole = user.getRole();
                user.setRole(userRole);
                user.setFullname(fullname != null ? fullname : user.getFullname());
                user.setProvider(registrationId.toUpperCase());

                // Fix username if it's null (for existing users)
                if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                    user.setUsername("google_user_" + googleId);
                    log.info("🔧 Fixed null username for existing user: {}", email);
                }

                userService.handleSaveUser(user);
                log.info("✅ Existing OAuth2 user updated: {}", email);
            }

            log.info("🎉 OAuth2 authentication successful for user: {} with role: {}", email, userRole.getName());
            return new DefaultOAuth2User(
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userRole.getName())),
                    oAuth2User.getAttributes(),
                    "email"); // Use email as the name attribute key

        } catch (OAuth2AuthenticationException e) {
            // Re-throw OAuth2AuthenticationException as-is
            throw e;
        } catch (Exception e) {
            log.error("💥 OAuth2 authentication failed: {}", e.getMessage(), e);
            OAuth2Error error = new OAuth2Error("authentication_failed", "OAuth2 authentication failed: " + e.getMessage(), null);
            throw new OAuth2AuthenticationException(error, e);
        }
    }

}
