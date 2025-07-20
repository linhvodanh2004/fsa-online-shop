package fsa.project.online_shop.services.implement;

import fsa.project.online_shop.models.Role;
import fsa.project.online_shop.models.User;
import fsa.project.online_shop.models.constant.UserRole;
import fsa.project.online_shop.services.RoleService;
import fsa.project.online_shop.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserService userService;
    private final RoleService roleService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String sub = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String fullname = (String) attributes.get("name");
        // Process avatar file for each provider

        // Set role admin for some specific account
        Role userRole = roleService.getRoleByName(UserRole.USER);

        User user = userService.getUserByEmail(email);
        // If user is null, add this oauth user to DB
        if (user == null) {
            User newUser = User.builder()
                    .role(userRole)
                    .status(true)
                    .fullname(fullname)
                    .email(email)
                    .username("google_user_" + sub)
                    .provider(registrationId.toUpperCase())
                    .build();
            userService.handleSaveUser(newUser);
        } else {
            // if user is not null, check it's enabled or not
            if (!user.getStatus()) {
                throw new OAuth2AuthenticationException("Account is locked");
            }
            userRole = user.getRole();
            user.setRole(userRole);
            user.setFullname(fullname);
            user.setProvider(registrationId.toUpperCase());
            userService.handleSaveUser(user);
        }
        return new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userRole.getName())),
                oAuth2User.getAttributes(),
                "sub");
    }

}
