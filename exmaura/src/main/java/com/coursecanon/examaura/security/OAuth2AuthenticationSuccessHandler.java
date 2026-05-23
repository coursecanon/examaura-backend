//package com.coursecanon.examaura.security;
//
//
//import com.coursecanon.examaura.entity.User;
//import com.coursecanon.examaura.entity.enums.OAuthProvider;
//import com.coursecanon.examaura.service.UserService;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//import org.springframework.web.util.UriComponentsBuilder;
//
//import java.io.IOException;
//import java.util.List;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
//
//    private final JwtTokenProvider tokenProvider;
//    private final UserService userService;
//
//    @Value("${app.oauth2.redirect-uri:http://localhost:3000/auth/callback}")
//    private String redirectUri;
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request,
//                                        HttpServletResponse response,
//                                        Authentication authentication)
//            throws IOException {
//
//        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
//        OAuth2User oAuth2User = oauthToken.getPrincipal();
//        String provider = oauthToken.getAuthorizedClientRegistrationId();
//
//        // Extract user info based on provider
//        String email = extractEmail(oAuth2User, provider);
//        String name = extractName(oAuth2User, provider);
//        String avatarUrl = extractAvatarUrl(oAuth2User, provider);
//        String oauthId = extractOAuthId(oAuth2User, provider);
//
//        // Create or update user
//        User user = userService.createOrUpdateOAuthUser(
//                email,
//                name,
//                avatarUrl,
//                OAuthProvider.valueOf(provider.toUpperCase()),
//                oauthId
//        );
//
//        // Generate JWT tokens
//        String accessToken = tokenProvider.generateAccessToken(
//                user.getId(),
//                user.getEmail(),
//                user.getName(),
//                List.of(new SimpleGrantedAuthority("ROLE_USER"))
//        );
//
//        String refreshToken = tokenProvider.generateRefreshToken(user.getId());
//
//        // Redirect to frontend with tokens
//        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
//                .queryParam("accessToken", accessToken)
//                .queryParam("refreshToken", refreshToken)
//                .build()
//                .toUriString();
//
//        getRedirectStrategy().sendRedirect(request, response, targetUrl);
//    }
//
//    private String extractEmail(OAuth2User oAuth2User, String provider) {
//        return switch (provider.toLowerCase()) {
//            case "google" -> oAuth2User.getAttribute("email");
//            case "github" -> oAuth2User.getAttribute("email");
//            default -> null;
//        };
//    }
//
//    private String extractName(OAuth2User oAuth2User, String provider) {
//        return switch (provider.toLowerCase()) {
//            case "google" -> oAuth2User.getAttribute("name");
//            case "github" -> oAuth2User.getAttribute("name");
//            default -> "User";
//        };
//    }
//
//    private String extractAvatarUrl(OAuth2User oAuth2User, String provider) {
//        return switch (provider.toLowerCase()) {
//            case "google" -> oAuth2User.getAttribute("picture");
//            case "github" -> oAuth2User.getAttribute("avatar_url");
//            default -> null;
//        };
//    }
//
//    private String extractOAuthId(OAuth2User oAuth2User, String provider) {
//        return switch (provider.toLowerCase()) {
//            case "google" -> oAuth2User.getAttribute("sub");
//            case "github" -> String.valueOf(oAuth2User.getAttribute("id"));
//            default -> null;
//        };
//    }
//}
