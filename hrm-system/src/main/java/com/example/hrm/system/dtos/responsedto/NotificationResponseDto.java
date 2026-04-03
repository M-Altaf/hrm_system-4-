package com.example.hrm.system.dtos.responsedto;





import com.example.hrm.system.emums.NotificationStatus;
import com.example.hrm.system.emums.NotificationType;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationResponseDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String title;
    private String message;
    private NotificationType type;
    private NotificationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    private String sentByName;
}