package com.coursecanon.examaura.dto.response;

import com.coursecanon.examaura.entity.enums.OAuthProvider;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("avatarUrl")
    private String avatar_url;
    private String fullName;
    private String refreshToken;


    private OAuthProvider oauthProvider;
//    private String oauthId;
}