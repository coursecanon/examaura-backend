package com.coursecanon.examaura.mapper;

import com.coursecanon.examaura.dto.request.UserUpdateRequestDto;
import com.coursecanon.examaura.dto.response.UserResponseDto;
import com.coursecanon.examaura.entity.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper {
    // Conerts a database User entity into a safe UserResponseDto for the client
    public UserResponseDto toResponse(User user){
        if (user == null){
            return null;
        }

        UserResponseDto response= new UserResponseDto();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setEnabled(user.getIsActive());
        response.setEmail(user.getEmail());

        //safely map roles
        // Note: change "role.name() to role.getName() or similar if role is custome entity instead of enum
        if (user.getUserRole() != null){
            response.setUserRole(user.getUserRole().name());
        }
        return response;
    }

    //Update existing user entity using data from UserUpdateRequestDTO
    public void updateEntityFromRequest(User existingUser, UserUpdateRequestDto request){
        if (existingUser == null || request == null){
            return;
        }
        if (request.getFullName() != null){
            existingUser.setFullName(request.getFullName());
        }

        if (request.getEmail() != null){
            existingUser.setEmail(request.getEmail());

        }
        //security note:
        //We deliberately DO NOT update the fields like username, password, roles or isEnabled here
        //Changing thoses fileds should be handled by dedicated, highly-secured endpoints
        //(e.g Authcontroller for passowrds, AdminController for roles)
    }
}
