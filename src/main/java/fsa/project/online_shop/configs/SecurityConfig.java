package fsa.project.online_shop.configs;

import fsa.project.online_shop.models.constant.UserRole;
import fsa.project.online_shop.services.UserService;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.session.security.web.authentication.SpringSessionRememberMeServices;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final AuthenticationSuccessHandler customSuccessAuthHandler;
    private final AuthenticationFailureHandler customFailureAuthHandler;

    private static final String[    ] PUBLIC_ENDPOINTS = {
            "/css/**", "/js/**", "/img/**", "/user/**", "/upload/**",
            "/", "/login", "/register", "/error/**",
    };
    private static final String[] AUTHENTICATED_ENDPOINTS = {
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SpringSessionRememberMeServices rememberMeServices() {
        SpringSessionRememberMeServices rememberMeServices = new SpringSessionRememberMeServices();
        // optionally customize
        rememberMeServices.setAlwaysRemember(false);
        return rememberMeServices;
    }

    @Bean
    public DaoAuthenticationProvider authProvider(
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(
                        auth -> auth
                                .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.INCLUDE).permitAll()
                                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                                .requestMatchers("/admin/**")
//                                .hasRole(UserRole.ADMIN)
                                .permitAll()
                                .anyRequest()
//                                .authenticated()
                                .permitAll()
                )
                .sessionManagement(
                        (sessionManagement) -> sessionManagement
                                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                                .invalidSessionUrl("/logout?expired")
                                .maximumSessions(1)
                                .maxSessionsPreventsLogin(false)
                )
                .logout(
                        logout -> logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/?success=logout-successfully")
                                .invalidateHttpSession(true)
                                .deleteCookies("JSESSIONID", "remember-me")
                                .invalidateHttpSession(true))
                .rememberMe(
                        (rememberMe) -> rememberMe
                                .key("fsa-online-shop-remember-me-key")
                                .tokenValiditySeconds(7 * 24 * 60 * 60) // 7 days
                                .rememberMeParameter("remember-me")
                                .rememberMeServices(rememberMeServices())
                )
                .formLogin(
                        formLogin -> formLogin
                                .loginPage("/login")
                                .failureUrl("/login?error")
                                .successHandler(customSuccessAuthHandler)
                                .failureHandler(customFailureAuthHandler)
                                .permitAll()

                )
                .exceptionHandling(
                        exception -> exception
                                .accessDeniedPage("/access-denied"));
        return http.build();

    }
}
