package com.poolapp.pool.dto;

import com.poolapp.pool.model.enums.RoleType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateDTO {

    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String password;
    private RoleType role;
}

