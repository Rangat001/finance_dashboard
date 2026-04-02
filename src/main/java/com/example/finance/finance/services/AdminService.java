package com.example.finance.finance.services;

import com.example.finance.finance.entity.records;
import com.example.finance.finance.entity.userEntity;
import com.example.finance.finance.repository.record_repository;
import com.example.finance.finance.repository.user_repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    user_repository user_repository;

    @Autowired
    record_repository record_repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean creteUser(userEntity user){
        try{
            user.setStatus(userEntity.Status.ACTIVE);
            //  hash the password
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            user_repository.save(user);
            return true;
        }catch(Exception e){
            System.out.println("Error creating user: " + e.getMessage());
        }
        return false;
    }

    public boolean UpdateUser(userEntity user,int id) {
        try {
            userEntity existingUser = user_repository.findById(id).orElse(null);
            if (existingUser == null) {
                return false;
            }

            // Update only non-null/non-blank fields
            if (user.getName() != null && !user.getName().isBlank()) {
                existingUser.setName(user.getName());
            }

            if (user.getEmail() != null && !user.getEmail().isBlank()) {
                userEntity emailOwner = user_repository.findByEmail(user.getEmail());
                if (emailOwner != null && emailOwner.getId() != existingUser.getId()) {
                    // Email already used by another user
                    return false;
                }
                existingUser.setEmail(user.getEmail());
            }

            // check is there is change in password or not
            if (user.getPassword() != null && !user.getPassword().isBlank() && existingUser.getPassword() != passwordEncoder.encode(user.getPassword()) ) {
                existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            }

            if (user.getRole() != null) {
                existingUser.setRole(user.getRole());
            }

//            if (user.getStatus() != null) {
//                existingUser.setStatus(user.getStatus());
//            }

            user_repository.save(existingUser);
            return true;
        } catch (Exception e) {
            System.out.println("Error updating user: " + e.getMessage());
            return false;
        }
    }

    public boolean Delete_User(int id){
        try{
        Optional<userEntity> user =  user_repository.findById(id);
        if(user != null && user.isPresent()){
            user.get().setStatus(userEntity.Status.INACTIVE);
            user_repository.save(user.get());
            return true;
        }
        }
        catch(Exception e){
            System.out.println("Error deleting user: " + e.getMessage());
        }
        return false;
    }


    //                                       Records
    public boolean createRecord(records record){
        try{
            record_repository.save(record);
            return true;
        }catch(Exception e){
            System.out.println("Error creating record: " + e.getMessage());
        }
        return false;
    }

    public boolean UpdateRecord(records record){
        try {
            record_repository.save(record);
            return true;
        }catch(Exception e){
            System.out.println("Error updating record: " + e.getMessage());
        }
        return  false;
    }


}
