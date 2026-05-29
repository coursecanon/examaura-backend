package com.coursecanon.examaura.security;

import com.coursecanon.examaura.entity.User;
import com.coursecanon.examaura.entity.enums.OAuthProvider;
import com.coursecanon.examaura.entity.enums.UserRole;
import com.coursecanon.examaura.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;


//This service fetches data from Google/Github and saves to DB
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Fetch the default OAuth2 profile from Google/GitHub
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. Identify which provider they used (google or github)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuthProvider authProvider = OAuthProvider.valueOf(registrationId.toUpperCase());

        // 3. Extract the data based on the provider's specific JSON structure
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = extractEmail(attributes, registrationId);
        String name = extractName(attributes, registrationId);
        String oauthId = extractId(attributes, registrationId);

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        // 4. Check if the user already exists in the database
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            // If the user exists but registered via a different method, throw an error or update them
            if (!user.getOauthProvider().equals(authProvider)) {
                throw new OAuth2AuthenticationException("Looks like you're signed up with " +
                        user.getOauthProvider() + " account. Please use your " + user.getOauthProvider() + " account to login.");
            }
            // Update existing user's details just in case they changed their name on Google/GitHub
            user.setFullName(name);
            user = userRepository.save(user);
        } else {

            // Extract username using new helper logic
            String username= extractUsername(attributes, registrationId, email);
            // 5. Register a brand-new social user
            user = User.builder()
                    .fullName(name)
                    .email(email)
                    .username(username)
                    .oauthProvider(authProvider)
                    .oauthId(oauthId)
                    .userRole(UserRole.VIEWER) // Default role for new signups
                    .build();
            user = userRepository.save(user);
        }

        // 6. Return our custom wrapper
        return new CustomOAuth2User(user, attributes);
    }

    // --- Helper Extraction Methods ---
    // Google and GitHub name their JSON keys differently, so we normalize them here.

    private String extractEmail(Map<String, Object> attributes, String registrationId) {
        if ("google".equalsIgnoreCase(registrationId)) return (String) attributes.get("email");
        if ("github".equalsIgnoreCase(registrationId)) return (String) attributes.get("email"); // Ensure GitHub scope includes user:email
        return null;
    }

    private String extractName(Map<String, Object> attributes, String registrationId) {
        if ("google".equalsIgnoreCase(registrationId)) return (String) attributes.get("name");
        if ("github".equalsIgnoreCase(registrationId)) {
            String name = (String) attributes.get("name");
            return (name != null) ? name : (String) attributes.get("login"); // Fallback to GitHub username
        }
        return null;
    }

    private String extractId(Map<String, Object> attributes, String registrationId) {
        if ("google".equalsIgnoreCase(registrationId)) return (String) attributes.get("sub");
        if ("github".equalsIgnoreCase(registrationId)) return String.valueOf(attributes.get("id"));
        return null;
    }

    //Extract Userame
    private String extractUsername(Map<String, Object> attributes, String registrationId, String email){

        String baseUsername;
        if ("github".equalsIgnoreCase(registrationId)){
            baseUsername= (String) attributes.get("login"); // extract usrname from github payload "login"
        }

        else if (email !=null && email.contains("@")){
            baseUsername= email.split("@")[0];
        }
        else {
            baseUsername= "user";
        }

        //remove any special characters or space just in case
        baseUsername=baseUsername.replaceAll("[^a-zA-Z0-9._-]","");

        // ===========================
        //COLLISION RESOLUTION LOOP
        //==========================
        String uniqueUsername= baseUsername;
        int counter=1;

        // Keep appending number untill we hit username that doesn't exist yet
        while (userRepository.existsByEmail(uniqueUsername)){
            uniqueUsername=baseUsername+counter;
            counter++;
        }

        return uniqueUsername;
    }
}

//What this achieves

//1. Normalization :  It solves classic OAuth headache where Google calls the unique ID "subs" but Github calls it "id"
//2. Account Collision Prevention : if someoe egistered with coursecanon@gmail.com via Local email/pass and later tries to click Login with Google,
// this service catches the mismatch and stops them, preventing database corruption
//3. Auto-Registration :  if they a brand new user, it automatically creates their row in the users table without requiring a seperate registration screen.

