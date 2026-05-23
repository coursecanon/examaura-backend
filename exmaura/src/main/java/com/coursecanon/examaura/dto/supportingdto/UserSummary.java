package com.coursecanon.examaura.dto.supportingdto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class UserSummary {
    private UUID id;
    private String name;
    private String email;
    private String avatarUrl;
}
