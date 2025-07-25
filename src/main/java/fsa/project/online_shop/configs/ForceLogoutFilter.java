package fsa.project.online_shop.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class ForceLogoutFilter extends OncePerRequestFilter {
    private static final List<String> LOGOUT_ENDPOINTS = List.of("/login", "/register", "/forgot-password"
            , "/reset-password", "/logout", "/resend-code");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (LOGOUT_ENDPOINTS.contains(path)
                && auth != null
                && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        else if (auth != null
                && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {
            Object principal = auth.getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                if (!userDetails.isEnabled()) {
                    new SecurityContextLogoutHandler().logout(request, response, auth);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
