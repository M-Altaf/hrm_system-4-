package com.example.hrm.system.dtos.updatedto;



import lombok.Data;

@Data
public class AttendanceReportDto {
    private Long employeeId;
    private String employeeName;
    private int month;
    private int year;
    private int totalWorkingDays;
    private int presentDays;
    private int absentDays;
    private int lateDays;
    private int halfDays;
    private int onLeaveDays;
    private double totalWorkingHours;
    private double attendancePercentage;
}
