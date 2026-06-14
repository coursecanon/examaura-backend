package com.coursecanon.examaura.dto.request;

import lombok.Data;

@Data
public class TokenRefreshRequestDTO {
    private String refreshToken;
}