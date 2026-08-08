package com.example.hrm.system.services;



import com.example.hrm.system.dtos.requestdto.SignupRequestDto;
import com.example.hrm.system.dtos.responsedto.UserResponseDto;
import com.example.hrm.system.entity.User;

import java.util.List;

public interface UserService {
    User registerUser(SignupRequestDto dto);
    List<UserResponseDto> getAllUsers();
}