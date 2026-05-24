package com.coursecanon.examaura.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class AuthenticationResponseDTO {
    private String token;
    private UUID userId;
    private String email;
    private String role;
}