package com.example.finance.finance.repository;

import com.example.finance.finance.entity.userEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface user_repository extends JpaRepository<userEntity, Integer> {
    userEntity findByEmail(String email);

}
