package com.example.finance.finance.controller;

import com.example.finance.finance.dto.record_dto;
import com.example.finance.finance.dto.user_dto;
import com.example.finance.finance.entity.records;
import com.example.finance.finance.entity.userEntity;
import com.example.finance.finance.services.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/Admin")

public class AdminController {

    @Autowired
    AdminService adminService;

    @GetMapping("/test")
    public String getMethodName() {
        String s = "Admin Controller is working correctly";
        return s;
    }

//                                        User Management
    @PostMapping("/addUser")
    public ResponseEntity<?> addUser(@Valid @RequestBody user_dto user) {

        if(adminService.creteUser(new userEntity(user.getName(),user.getRole(),user.getEmail(),user.getPassword()))){
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PutMapping("/update-user/{id}")
    public ResponseEntity<?> updateUser(@Valid @RequestBody user_dto user, @PathVariable("id") int id) {
        if(adminService.UpdateUser(new userEntity(user.getName(),user.getRole(),user.getEmail(),user.getPassword()),id)){
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable int id) {
        boolean op = adminService.Delete_User(id);
        if(op){
            return new ResponseEntity<>(HttpStatus.OK);

        }
        return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);


    }



//                                        Record Management

    @PostMapping("/addRecord")
    public ResponseEntity<?> addRecord(@Valid @RequestBody record_dto record) {

        if(adminService.createRecord(new records(record.getAmount(),record.getCategory(),record.getNote(),record.getDate(),record.getType()))){
            return new ResponseEntity<>(HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @PutMapping("/record/{id}")
    public ResponseEntity<?> updateRecord(@Valid @RequestBody record_dto record, @PathVariable int id) {
        records r = new records(record.getAmount(),record.getCategory(),record.getNote(),record.getDate(),record.getType());
        r.setId(id);
        if(adminService.UpdateRecord(r)){
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }




}
