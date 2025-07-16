package fsa.project.online_shop.configs;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;

@Configuration
public class CustomFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String errorMessage = "";
        if (exception.getClass().isAssignableFrom(BadCredentialsException.class)
                || exception.getClass().isAssignableFrom(UsernameNotFoundException.class)) {
            errorMessage = "bad-credentials";
        } else if (exception.getClass().isAssignableFrom(DisabledException.class)) {
            errorMessage = "disabled";
        } else {
            errorMessage = "login-error";
        }
        response.sendRedirect(request.getContextPath() + "/login?error=" + errorMessage);
    }
}

