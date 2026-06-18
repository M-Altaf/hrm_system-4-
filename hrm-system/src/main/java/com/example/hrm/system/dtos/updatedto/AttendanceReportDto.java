package com.example.hrm.system.dtos.updatedto;


import lombok.Data;

@Data
public class AttendanceReportDto {

    private Long employeeId;
    private String employeeName;

    private Integer month;
    private Integer year;

    private Long totalDays;           // Total working days (excluding weekends)
    private Long presentDays;
    private Long lateDays;
    private Long absentDays;
    private Long halfDayCount;

    private Double totalWorkingHours;

    // Calculated fields
    private Double attendancePercentage;
    private Double averageWorkingHoursPerDay;

    // Calculate Attendance Percentage
    public Double getAttendancePercentage() {
        if (totalDays == null || totalDays == 0) return 0.0;
        long attended = (presentDays != null ? presentDays : 0) +
                (lateDays != null ? lateDays : 0);
        return Math.round((attended * 100.0) / totalDays * 100.0) / 100.0;
    }

    // Calculate Average Working Hours
    public Double getAverageWorkingHoursPerDay() {
        if (presentDays == null || presentDays == 0) return 0.0;
        return Math.round((totalWorkingHours != null ? totalWorkingHours : 0.0) / presentDays * 100.0) / 100.0;
    }
}