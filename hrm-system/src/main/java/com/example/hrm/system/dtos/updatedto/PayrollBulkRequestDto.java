package com.example.hrm.system.dtos.updatedto;


import lombok.Data;

@Data
public class PayrollBulkRequestDto {
    private Integer month;
    private Integer year;
    private Double bonusForAll;         // optional bonus for everyone
}
