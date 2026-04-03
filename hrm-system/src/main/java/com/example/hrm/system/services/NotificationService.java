package com.example.hrm.system.services;



import com.example.hrm.system.dtos.requestdto.NotificationBulkRequestDto;
import com.example.hrm.system.dtos.requestdto.NotificationRequestDto;
import com.example.hrm.system.dtos.responsedto.NotificationResponseDto;
import com.example.hrm.system.emums.NotificationType;


import java.util.List;

public interface NotificationService {

    // Send
    NotificationResponseDto sendNotification(NotificationRequestDto dto);
    List<NotificationResponseDto> sendBulkNotification(NotificationBulkRequestDto dto);

    // Internal auto notifications
    void sendLeaveNotification(Long employeeId, String status, String leaveType);
    void sendPayrollNotification(Long employeeId, int month, int year, Double netSalary);
    void sendAttendanceWarning(Long employeeId, int lateDays);

    // Read
    NotificationResponseDto markAsRead(Long notificationId);
    List<NotificationResponseDto> markAllAsRead(Long employeeId);
    List<NotificationResponseDto> getByEmployee(Long employeeId);
    List<NotificationResponseDto> getUnreadByEmployee(Long employeeId);
    List<NotificationResponseDto> getByEmployeeAndType(Long employeeId, NotificationType type);
    long getUnreadCount(Long employeeId);

    // Delete
    void deleteNotification(Long notificationId);
    void deleteAllByEmployee(Long employeeId);
}
