package com.coursecanon.examaura.dto.response;

import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class UserResponseDto {
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private String userRole;
    private String avatarUrl;
    private boolean enabled;

}
