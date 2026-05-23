# Security Configuration
## OAuth2 + JWT Implementation Guide

---

## Table of Contents
1. [Security Architecture](#security-architecture)
2. [OAuth2 Configuration](#oauth2-configuration)
3. [JWT Token Management](#jwt-token-management)
4. [Spring Security Configuration](#spring-security-configuration)
5. [Implementation Code](#implementation-code)

---

## Security Architecture

### Authentication Flow

```
┌──────────┐                           ┌────────────┐                      ┌──────────────┐
│          │  1. Login with Google     │            │  2. Redirect to      │              │
│ Frontend ├─────────────────────────> │  Backend   ├───────────────────>  │   Google     │
│          │                           │   /auth    │                      │   OAuth2     │
└────┬─────┘                           └─────┬──────┘                      └──────┬───────┘
     │                                       │                                    │
     │                                       │  4. Exchange code for token        │
     │                                       │  <────────────────────────────────┤
     │                                       │                                    │
     │  6. Return JWT + User Info            │  5. Create/Update User            │
     │  <────────────────────────────────────┤     Generate JWT                  │
     │                                       │                                    │
     │  7. Subsequent API calls              │                                    │
     │     with JWT in Authorization header  │                                    │
     │  ─────────────────────────────────> │                                    │
     │                                       │                                    │
     │  8. Validate JWT & process request    │                                    │
     │  <─────────────────────────────────  │                                    │
     │                                       │                                    │
```

### Security Layers

1. **OAuth2 Authentication** - Google/GitHub identity verification
2. **JWT Authorization** - Stateless session management
3. **CSRF Protection** - Cross-site request forgery prevention
4. **CORS Configuration** - Cross-origin resource sharing
5. **Rate Limiting** - API abuse prevention
6. **Role-Based Access Control (RBAC)** - Permission management

---

## OAuth2 Configuration

### application.yml

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope:
              - email
              - profile
            redirect-uri: "{baseUrl}/api/v1/auth/oauth2/callback/google"
            client-name: Google
            
          github:
            client-id: ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}
            scope:
              - user:email
              - read:user
            redirect-uri: "{baseUrl}/api/v1/auth/oauth2/callback/github"
            client-name: GitHub
            
        provider:
          google:
            authorization-uri: https://accounts.google.com/o/oauth2/v2/auth
            token-uri: https://oauth2.googleapis.com/token
            user-info-uri: https://www.googleapis.com/oauth2/v3/userinfo
            user-name-attribute: sub
            
          github:
            authorization-uri: https://github.com/login/oauth/authorize
            token-uri: https://github.com/login/oauth/access_token
            user-info-uri: https://api.github.com/user
            user-name-attribute: id

# JWT Configuration
jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-change-this-in-production}
  access-token-expiration: 3600000  # 1 hour in milliseconds
  refresh-token-expiration: 604800000  # 7 days in milliseconds
  issuer: quizmaster-api
  
# CORS Configuration
cors:
  allowed-origins:
    - http://localhost:3000
    - http://localhost:5173
    - https://quizmaster.com
  allowed-methods:
    - GET
    - POST
    - PUT
    - DELETE
    - OPTIONS
  allowed-headers:
    - "*"
  exposed-headers:
    - Authorization
  allow-credentials: true
  max-age: 3600
```

---

## JWT Token Management

### JwtTokenProvider.java

```java
package com.quizmaster.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final String issuer;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration,
            @Value("${jwt.issuer}") String issuer
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.issuer = issuer;
    }

    /**
     * Generate JWT access token
     */
    public String generateAccessToken(UUID userId, String email, String name, 
                                     Collection<? extends GrantedAuthority> authorities) {
        Instant now = Instant.now();
        Instant expiryDate = now.plusMillis(accessTokenExpiration);

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("name", name);
        claims.put("roles", authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiryDate))
                .addClaims(claims)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Generate JWT refresh token
     */
    public String generateRefreshToken(UUID userId) {
        Instant now = Instant.now();
        Instant expiryDate = now.plusMillis(refreshTokenExpiration);

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiryDate))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract user ID from JWT token
     */
    public UUID getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return UUID.fromString(claims.getSubject());
    }

    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }

    /**
     * Extract all claims from token
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
```

---

## Spring Security Configuration

### SecurityConfig.java

```java
package com.quizmaster.config;

import com.quizmaster.security.JwtAuthenticationFilter;
import com.quizmaster.security.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final OAuth2AuthenticationSuccessHandler oauth2SuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable()) // Disabled for JWT; use CSRF tokens if using sessions
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(HttpMethod.GET, 
                    "/api/v1/quizzes",
                    "/api/v1/quizzes/**",
                    "/api/v1/categories",
                    "/api/v1/categories/**"
                ).permitAll()
                
                // Auth endpoints
                .requestMatchers(
                    "/api/v1/auth/**",
                    "/oauth2/**"
                ).permitAll()
                
                // Swagger/OpenAPI documentation
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                
                // Health check
                .requestMatchers("/actuator/health").permitAll()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(endpoint -> 
                    endpoint.baseUri("/api/v1/auth/oauth2/authorize")
                )
                .redirectionEndpoint(endpoint -> 
                    endpoint.baseUri("/api/v1/auth/oauth2/callback/*")
                )
                .successHandler(oauth2SuccessHandler)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:5173",
            "https://quizmaster.com"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### JwtAuthenticationFilter.java

```java
package com.quizmaster.security;

import com.quizmaster.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) 
            throws ServletException, IOException {
        
        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                UUID userId = tokenProvider.getUserIdFromToken(jwt);
                UserDetails userDetails = userService.loadUserById(userId);

                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, 
                        null, 
                        userDetails.getAuthorities()
                    );
                    
                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }
}
```

### OAuth2AuthenticationSuccessHandler.java

```java
package com.quizmaster.security;

import com.quizmaster.entity.User;
import com.quizmaster.entity.enums.OAuthProvider;
import com.quizmaster.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    @Value("${app.oauth2.redirect-uri:http://localhost:3000/auth/callback}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                       HttpServletResponse response, 
                                       Authentication authentication) 
            throws IOException {
        
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();
        String provider = oauthToken.getAuthorizedClientRegistrationId();

        // Extract user info based on provider
        String email = extractEmail(oAuth2User, provider);
        String name = extractName(oAuth2User, provider);
        String avatarUrl = extractAvatarUrl(oAuth2User, provider);
        String oauthId = extractOAuthId(oAuth2User, provider);

        // Create or update user
        User user = userService.createOrUpdateOAuthUser(
            email, 
            name, 
            avatarUrl, 
            OAuthProvider.valueOf(provider.toUpperCase()),
            oauthId
        );

        // Generate JWT tokens
        String accessToken = tokenProvider.generateAccessToken(
            user.getId(),
            user.getEmail(),
            user.getName(),
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());

        // Redirect to frontend with tokens
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String extractEmail(OAuth2User oAuth2User, String provider) {
        return switch (provider.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("email");
            case "github" -> oAuth2User.getAttribute("email");
            default -> null;
        };
    }

    private String extractName(OAuth2User oAuth2User, String provider) {
        return switch (provider.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("name");
            case "github" -> oAuth2User.getAttribute("name");
            default -> "User";
        };
    }

    private String extractAvatarUrl(OAuth2User oAuth2User, String provider) {
        return switch (provider.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("picture");
            case "github" -> oAuth2User.getAttribute("avatar_url");
            default -> null;
        };
    }

    private String extractOAuthId(OAuth2User oAuth2User, String provider) {
        return switch (provider.toLowerCase()) {
            case "google" -> oAuth2User.getAttribute("sub");
            case "github" -> String.valueOf(oAuth2User.getAttribute("id"));
            default -> null;
        };
    }
}
```

---

## Implementation Code

### AuthController.java

```java
package com.quizmaster.controller;

import com.quizmaster.dto.request.RefreshTokenRequest;
import com.quizmaster.dto.response.AuthResponse;
import com.quizmaster.dto.response.UserResponse;
import com.quizmaster.security.JwtTokenProvider;
import com.quizmaster.service.AuthService;
import com.quizmaster.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @RequestBody RefreshTokenRequest request) {
        
        AuthResponse response = authService.refreshAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID userId = UUID.fromString(userDetails.getUsername());
        UserResponse user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // With JWT, logout is handled client-side by removing tokens
        // Optionally implement token blacklist if needed
        return ResponseEntity.ok().build();
    }
}
```

---

## Environment Variables

### Required Environment Variables

```bash
# OAuth2 Providers
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GITHUB_CLIENT_ID=your-github-client-id
GITHUB_CLIENT_SECRET=your-github-client-secret

# JWT Configuration
JWT_SECRET=your-256-bit-secret-change-in-production-must-be-long-enough
JWT_ACCESS_TOKEN_EXPIRATION=3600000  # 1 hour
JWT_REFRESH_TOKEN_EXPIRATION=604800000  # 7 days

# Frontend URL for CORS and OAuth redirects
FRONTEND_URL=http://localhost:3000
```

---

## Security Best Practices

### 1. JWT Secret Management
- Use strong, randomly generated secrets (at least 256 bits)
- Store secrets in environment variables or secret managers (AWS Secrets Manager, Azure Key Vault)
- Rotate secrets periodically

### 2. Token Expiration
- Keep access token expiration short (15-60 minutes)
- Use refresh tokens for extended sessions
- Implement token revocation for logout

### 3. HTTPS Only
- Enforce HTTPS in production
- Set secure flags on cookies if used
- Use HSTS headers

### 4. Rate Limiting
- Implement rate limiting on authentication endpoints
- Use tools like Bucket4j or Redis rate limiter

### 5. Input Validation
- Validate all user inputs
- Use Bean Validation annotations
- Sanitize data to prevent injection attacks

---

**Version:** 1.0  
**Last Updated:** May 12, 2026
