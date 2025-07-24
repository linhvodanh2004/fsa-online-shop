package fsa.project.online_shop.configs;

import fsa.project.online_shop.models.User;
import fsa.project.online_shop.models.constant.UserRole;
import fsa.project.online_shop.services.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class CustomSuccessHandler implements AuthenticationSuccessHandler {
    private final UserService userService;
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(CustomSuccessHandler.class);
    protected String determineTargetUrl(final Authentication authentication) {
        Map<String, String> roleTargetUrlMap = new HashMap<>();
        roleTargetUrlMap.put("ROLE_" + UserRole.USER, "/");
        roleTargetUrlMap.put("ROLE_" + UserRole.ADMIN, "/admin/dashboard");

        final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (final GrantedAuthority grantedAuthority : authorities) {
            String authorityName = grantedAuthority.getAuthority();
            if (roleTargetUrlMap.containsKey(authorityName)) {
                return roleTargetUrlMap.get(authorityName);
            }
        }
        throw new IllegalStateException();
    }

    protected void clearAuthenticationAttributes(HttpSession session) {
        if (session == null) {
            return;
        }
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
        HttpSession session = request.getSession(false);
        String targetUrl = determineTargetUrl(authentication);
        if (response.isCommitted()) {
            return;
        }
        // Get username from Spring Security Context Holder
        User user;
        if(authentication.getPrincipal() instanceof OAuth2User oAuth2User){
            String email = oAuth2User.getAttribute("email");
            user = userService.findByEmail(email);
        }
        else {
            // For form login, authentication.getName() returns the email (from CustomUserDetailsService)
            String email = authentication.getName();
            user = userService.findByEmail(email);
        }
        logger.info("User '{}' (ID: {}, Email: {}, Provider: {}) logged in with authorities: {}",
                user.getUsername(), user.getId(), user.getEmail(), user.getProvider(), authentication.getAuthorities());
        session.setAttribute("fullname", user.getFullname());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("userId", user.getId());
        redirectStrategy.sendRedirect(request, response, targetUrl);
        clearAuthenticationAttributes(session);
    }
}
