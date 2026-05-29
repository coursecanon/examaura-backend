package com.coursecanon.examaura.config; // or .security

import com.coursecanon.examaura.security.CustomOAuth2UserService;
import com.coursecanon.examaura.security.JwtAuthenticationFilter;
import com.coursecanon.examaura.security.OAuth2AuthenticationSuccessHandler;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    private  final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Disable CSRF since we are using JWTs, not session cookies
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Configure endpoint permissions
                .authorizeHttpRequests(auth -> auth
                        //Allow all internal forwards and error dispatches
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                        // Make login, register, and Swagger UI completely public
                        .requestMatchers(
                                "/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/error",
                                "/oauth2/**",
                                "/login/**"
                        ).permitAll()
                        // Lock down everything else
                        .anyRequest().authenticated()
                )

//                 REQUIRED for Google/GitHub login
//                .oauth2Login(Customizer.withDefaults())

                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                )

                // Prevent redirection when erorr happens in jwt registration
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write(String.format("{\"status\": 401, \"error\": \"Unauthorized\", \"message\": \"%s\"}", authException.getMessage()));
                        })))

                // 3. Make the session stateless (Spring won't store session data, requiring a JWT every time)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4. Register our custom provider and JWT filter
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}