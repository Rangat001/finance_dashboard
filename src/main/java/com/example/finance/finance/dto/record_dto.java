package com.example.finance.finance.dto;

import com.example.finance.finance.entity.records;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class record_dto {

    @Positive(message = "amount must be a positive number")
    private Float amount;

    @NotBlank(message = "category is required")
    private String category;

    @NotBlank(message = "Note is required")
    private String note;

    @NotNull(message = "date is required")
    @PastOrPresent(message = "date cannot be in the future")
    private Date date;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "type is required")
    private records.Type type;


}
