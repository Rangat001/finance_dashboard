package com.example.finance.finance.dto;

import com.example.finance.finance.entity.userEntity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class user_dto {
    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "password is required")
    private String password;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    private String email;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "role is required")
    private userEntity.Role role;


}
