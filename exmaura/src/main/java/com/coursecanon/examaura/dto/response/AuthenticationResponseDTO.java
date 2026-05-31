package com.coursecanon.examaura.dto.response;

import com.coursecanon.examaura.entity.enums.OAuthProvider;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class AuthenticationResponseDTO {
    private String token;
    private UUID userId;
    private String email;
    private String username;
    private String role;
    private String avatar_url;
    private String fullName;


    private OAuthProvider oauthProvider;
//    private String oauthId;
}