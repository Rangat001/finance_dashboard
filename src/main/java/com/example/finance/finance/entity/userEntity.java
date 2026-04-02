package com.example.finance.finance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

@Entity
@Data
@Table(name = "users")

public class userEntity {

    @Id
    private int id;


    private String name;


    private String password;



    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private  Status status;

    public userEntity() {

    }


    public enum Status{
        ACTIVE,
        INACTIVE
    }

    public enum Role{
        ADMIN,
        ANALYST,
        VIEWER
    }

    public userEntity(String username, Role role,String email,String password) {
        this.email =  email;
        this.password = password;
        this.role = role;
        this.name =  username;
        this.status=Status.ACTIVE;
    }


}
