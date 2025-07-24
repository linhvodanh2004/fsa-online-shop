package fsa.project.online_shop.configs;

import fsa.project.online_shop.models.constant.UserRole;
import fsa.project.online_shop.services.UserService;
import fsa.project.online_shop.services.implement.CustomOAuth2UserService;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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
    private final CustomOAuth2UserService customOAuth2UserService;
    private final AuthenticationSuccessHandler customSuccessAuthHandler;
    private final AuthenticationFailureHandler customFailureAuthHandler;

    private static final String[] PUBLIC_ENDPOINTS = {
            "/css/**", "/js/**", "/img/**", "/user/**", "/upload/**", "/productImg/**", "/resend-code",
            "/", "/login", "/register", "/error/**", "/reset-password", "/logout", "/forgot-password",
            "/cart/**", "/shop/**", "/contact", "/about", "/shop-category/**", "/shop-single/**",
            "/api/**",  // Allow API endpoints for chat
            "/product/**"  // Allow product slug URLs with /product/ prefix
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
                                .requestMatchers("/admin/**").permitAll()
                                .anyRequest().permitAll()
                )
                .sessionManagement(
                        (sessionManagement) -> sessionManagement
                                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                                .invalidSessionUrl("/?success=session-expired")
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
                                .key("7dZH1BivDnbMqsbswVO4925Hgtd3Pzmm")
                                .tokenValiditySeconds(7 * 24 * 60 * 60) // 7 days
                                .rememberMeParameter("remember-me")
                                .rememberMeServices(rememberMeServices())
                )
                .formLogin(
                        formLogin -> formLogin
                                .loginPage("/login")
                                .failureUrl("/login?error=login-error")
                                .successHandler(customSuccessAuthHandler)
                                .failureHandler(customFailureAuthHandler)
                                .permitAll()

                )
                .oauth2Login(
                        oauth2 -> oauth2.loginPage("/login")
                                .defaultSuccessUrl("/", true)
                                .failureUrl("/login?error=login-error")
                                .userInfoEndpoint(userInfoEndpointConfig ->
                                        userInfoEndpointConfig.userService(customOAuth2UserService))
                                .successHandler(customSuccessAuthHandler)
                                .failureHandler(customFailureAuthHandler)
                )
                .exceptionHandling(
                        exception -> exception
                                .accessDeniedPage("/access-denied"))
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();

    }
}
