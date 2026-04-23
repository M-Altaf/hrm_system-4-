package com.example.hrm.system.services;


import com.example.hrm.system.dtos.requestdto.SignupRequestDto;
import com.example.hrm.system.entity.User;

public interface UserService {
    User registerUser(SignupRequestDto dto);

}