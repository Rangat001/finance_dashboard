package com.example.finance.finance.services;

import com.example.finance.finance.entity.records;
import com.example.finance.finance.repository.record_repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ViewerService {

    @Autowired
    record_repository record_repository;


    public List<records> getAllRecords(){
        try {
            List<records> list = record_repository.findAll();
            return list;
        }catch (Exception e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
