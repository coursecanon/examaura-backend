package com.coursecanon.examaura.security;

import com.coursecanon.examaura.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Extract the Authorization header
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. Check if the header is missing or doesn't start with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Pass to the next filter, but unauthenticated
            return;
        }

        // 3. Extract the token (Remove "Bearer " from the string)
        jwt = authHeader.substring(7);

        // 4. Extract the user email from the token
        userEmail = jwtService.extractUsername(jwt);



        // 5. If we have an email, and the user isn't already authenticated in this session
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Fetch the user from the database
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 6. Validate the token against the database user
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // 🚀 6.5 VERSION VALIDATION
                // Cast to your User entity to access the tokenVersion field
                if (userDetails instanceof com.coursecanon.examaura.entity.User) {
                    Integer dbVersion = ((User) userDetails).getTokenVersion();
                    Integer jwtVersion = jwtService.extractTokenVersion(jwt); // You need to implement this in JwtService

                    if (!dbVersion.equals(jwtVersion)) {
                        // Password was changed, token is now obsolete
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session expired due to password change.");
                        return;
                    }
                }

                // 7. Create an authentication object
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // No credentials needed here, token is proof
                        userDetails.getAuthorities() // Roles (e.g., ROLE_ADMIN)
                );

                // Add details about the web request (like IP address, session ID)
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 8. Update the Security Context
                // This tells Spring Security: "This user is fully authenticated for this request."
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 9. Continue the filter chain
        filterChain.doFilter(request, response);
        System.out.println(
                request.getMethod() + " " + request.getRequestURI()
        );
    }
}