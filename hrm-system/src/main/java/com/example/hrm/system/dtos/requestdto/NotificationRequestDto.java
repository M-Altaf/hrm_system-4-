package com.example.hrm.system.dtos.requestdto;


import com.example.hrm.system.emums.NotificationType;
import lombok.Data;

@Data
public class NotificationRequestDto {
    private Long employeeId;
    private String title;
    private String message;
    private NotificationType type;
    private Long sentById;              // HR/Admin user id
}