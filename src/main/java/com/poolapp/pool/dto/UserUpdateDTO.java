package com.poolapp.pool.dto;

import com.poolapp.pool.model.enums.RoleType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UserUpdateDTO {

    @Email
    private String email;

    @Size(min = 1, max = 50)
    private String firstName;

    @Size(min = 1, max = 50)
    private String lastName;

    @Pattern(regexp = "^\\+?[0-9\\- ]{7,20}$")
    private String phoneNumber;

    @Size(min = 6, max = 100)
    private String password;

    private RoleType role;
}

