package com.example.finance.finance.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table(name="records")

public class records {

    @Id
    private int id;


    private Float amount;
    private String category;
    private String note;
    private Date date;

    @Enumerated(EnumType.STRING)
    private Type type;

    public enum  Type{
        INCOME,
        EXPENSE
    }

    public records(){

    }
    public records(Float amount, String category, String note, Date date, Type type) {
        this.amount = amount;
        this.category = category;
        this.note = note;
        this.date = date;
        this.type = type;
    }

}
