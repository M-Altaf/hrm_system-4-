package com.example.hrm.system.dtos.requestdto;


import com.example.hrm.system.emums.NotificationType;
import lombok.Data;

@Data
public class NotificationBulkRequestDto {
    private Long departmentId;          // null = send to ALL employees
    private String title;
    private String message;
    private NotificationType type;
    private Long sentById;
}