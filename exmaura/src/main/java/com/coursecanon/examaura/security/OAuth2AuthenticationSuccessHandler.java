package com.coursecanon.examaura.security;

import com.coursecanon.examaura.entity.RefreshToken;
import com.coursecanon.examaura.entity.User;
import com.coursecanon.examaura.repository.UserRepository;
import com.coursecanon.examaura.service.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// The OAuthAuthenticationSuccessHandler intercepts that moment of trimph. its job is to grab the user, mint a fresh JWT and aggressively redirect the browser back to
//React frontend with token attached to URL

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    // We default to localhost:5173 for React, but allow overriding via application.yml for production
    @Value("${app.frontend.redirect-uri}")
    private String frontendRedirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        // 1. Extract our custom user wrapper from the authentication object
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

// 1. Extract the real-time attributes from Google
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture"); // 👈 Google's profile picture URL

        User dbUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("OAuth2 user not found in database"));



        // 2. Pack them into an extra claims map
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("name", name);
        extraClaims.put("avatarUrl", picture); // Passing it directly to the token without saving to DB
        extraClaims.put("userId", dbUser.getId());
        extraClaims.put("tokenVersion", dbUser.getTokenVersion());
        if (dbUser.getUserRole() != null) {
            extraClaims.put("role", dbUser.getUserRole().name());
        }
        String token = jwtService.generateToken(extraClaims, dbUser);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(email);

        // 3. Construct the redirect URL (e.g., http://localhost:3000/oauth2/redirect?token=eyJhbGci...)
        String targetUrl = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("token", token)
                .queryParam("refreshToken", refreshToken.getToken())
                .build().toUriString();

        // 4. Clear any old authentication attributes and execute the redirect
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}