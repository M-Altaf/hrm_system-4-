package com.example.hrm.system.dtos.updatedto;

import lombok.Data;

@Data
public class AttendanceReportDto {

    private Long employeeId;
    private String employeeName;
    private Integer month;
    private Integer year;
    private Long totalDays;
    private Long presentDays;
    private Long lateDays;
    private Long absentDays;
    private Double totalWorkingHours;

    // Optional: Add calculated fields
    private Double attendancePercentage;
    private Double averageWorkingHoursPerDay;

    // You can add a method to calculate percentages
    public Double getAttendancePercentage() {
        if (totalDays == null || totalDays == 0) return 0.0;
        return (presentDays * 100.0) / totalDays;
    }

    public Double getAverageWorkingHoursPerDay() {
        if (presentDays == null || presentDays == 0) return 0.0;
        return totalWorkingHours / presentDays;
    }
}