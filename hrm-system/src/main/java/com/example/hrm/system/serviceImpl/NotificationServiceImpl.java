package com.example.hrm.system.serviceImpl;



import com.example.hrm.system.dtos.requestdto.NotificationBulkRequestDto;
import com.example.hrm.system.dtos.requestdto.NotificationRequestDto;
import com.example.hrm.system.dtos.responsedto.NotificationResponseDto;
import com.example.hrm.system.emums.NotificationStatus;
import com.example.hrm.system.emums.NotificationType;
import com.example.hrm.system.entity.Employee;
import com.example.hrm.system.entity.Notification;
import com.example.hrm.system.entity.User;
import com.example.hrm.system.exeption.ResourceNotFoundException;
import com.example.hrm.system.repository.EmployeeRepository;
import com.example.hrm.system.repository.NotificationRepository;
import com.example.hrm.system.repository.UserRepository;
import com.example.hrm.system.services.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            EmployeeRepository employeeRepository,
            UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }

    // ── SEND SINGLE NOTIFICATION ─────────────────────────────────────

    @Override
    @Transactional
    public NotificationResponseDto sendNotification(
            NotificationRequestDto dto) {

        // 1. Check employee exists
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found: " + dto.getEmployeeId()));

        // 2. Get sender
        User sentBy = null;
        if (dto.getSentById() != null) {
            sentBy = userRepository.findById(dto.getSentById())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found: " + dto.getSentById()));
        }

        // 3. Build and save notification
        Notification notification = buildNotification(
                employee,
                dto.getTitle(),
                dto.getMessage(),
                dto.getType(),
                sentBy
        );

        return mapToDto(notificationRepository.save(notification));
    }

    // ── SEND BULK NOTIFICATION ───────────────────────────────────────

    @Override
    @Transactional
    public List<NotificationResponseDto> sendBulkNotification(
            NotificationBulkRequestDto dto) {

        // Get employees — by department or all
        List<Employee> employees;
        if (dto.getDepartmentId() != null) {
            employees = employeeRepository
                    .findByDepartmentId(dto.getDepartmentId());
            if (employees.isEmpty()) {
                throw new ResourceNotFoundException(
                        "No employees found in department: "
                                + dto.getDepartmentId());
            }
        } else {
            employees = employeeRepository.findAll();
        }

        // Get sender
        User sentBy = null;
        if (dto.getSentById() != null) {
            sentBy = userRepository.findById(dto.getSentById())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found: " + dto.getSentById()));
        }

        // Send to each employee
        List<NotificationResponseDto> results = new ArrayList<>();
        User finalSentBy = sentBy;

        for (Employee employee : employees) {
            Notification notification = buildNotification(
                    employee,
                    dto.getTitle(),
                    dto.getMessage(),
                    dto.getType(),
                    finalSentBy
            );
            results.add(mapToDto(notificationRepository.save(notification)));
        }

        return results;
    }

    // ── AUTO — LEAVE NOTIFICATION ────────────────────────────────────

    @Override
    @Transactional
    public void sendLeaveNotification(Long employeeId,
                                      String status,
                                      String leaveType) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found: " + employeeId));

        String title;
        String message;

        switch (status) {
            case "APPROVED" -> {
                title   = "Leave Approved ✅";
                message = "Your " + leaveType
                        + " leave request has been approved. "
                        + "Enjoy your time off!";
            }
            case "REJECTED" -> {
                title   = "Leave Rejected ❌";
                message = "Your " + leaveType
                        + " leave request has been rejected. "
                        + "Please check the rejection reason.";
            }
            case "PENDING" -> {
                title   = "Leave Request Submitted 📝";
                message = "Your " + leaveType
                        + " leave request has been submitted "
                        + "and is pending approval.";
            }
            default -> {
                title   = "Leave Status Update";
                message = "Your leave status has been updated to: " + status;
            }
        }

        Notification notification = buildNotification(
                employee, title, message,
                NotificationType.LEAVE, null
        );
        notificationRepository.save(notification);
    }

    // ── AUTO — PAYROLL NOTIFICATION ──────────────────────────────────

    @Override
    @Transactional
    public void sendPayrollNotification(Long employeeId,
                                        int month, int year,
                                        Double netSalary) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found: " + employeeId));

        String[] months = {
                "January", "February", "March", "April",
                "May", "June", "July", "August",
                "September", "October", "November", "December"
        };

        String title   = "Payroll Generated 💰";
        String message = "Your payroll for "
                + months[month - 1] + " " + year
                + " has been generated. "
                + "Net salary: PKR " + String.format("%.2f", netSalary);

        Notification notification = buildNotification(
                employee, title, message,
                NotificationType.PAYROLL, null
        );
        notificationRepository.save(notification);
    }

    // ── AUTO — ATTENDANCE WARNING ────────────────────────────────────

    @Override
    @Transactional
    public void sendAttendanceWarning(Long employeeId, int lateDays) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found: " + employeeId));

        String title   = "Attendance Warning ⚠️";
        String message = "You have been late " + lateDays
                + " times this month. "
                + "Please ensure punctuality to avoid salary deductions.";

        Notification notification = buildNotification(
                employee, title, message,
                NotificationType.WARNING, null
        );
        notificationRepository.save(notification);
    }

    // ── MARK AS READ ─────────────────────────────────────────────────

    @Override
    @Transactional
    public NotificationResponseDto markAsRead(Long notificationId) {
        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Notification not found: " + notificationId));

        if (notification.getStatus() == NotificationStatus.READ) {
            throw new IllegalArgumentException(
                    "Notification is already marked as READ");
        }

        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(LocalDateTime.now());

        return mapToDto(notificationRepository.save(notification));
    }

    // ── MARK ALL AS READ ─────────────────────────────────────────────

    @Override
    @Transactional
    public List<NotificationResponseDto> markAllAsRead(Long employeeId) {
        List<Notification> unread = notificationRepository
                .findByEmployeeIdAndStatus(
                        employeeId, String.valueOf(NotificationStatus.UNREAD));

        if (unread.isEmpty()) {
            throw new IllegalArgumentException(
                    "No unread notifications found for employee: "
                            + employeeId);
        }

        unread.forEach(n -> {
            n.setStatus(NotificationStatus.READ);
            n.setReadAt(LocalDateTime.now());
        });

        return notificationRepository.saveAll(unread)
                .stream().map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ── GET METHODS ──────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getByEmployee(Long employeeId) {
        return notificationRepository.findByEmployeeId(employeeId)
                .stream().map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getUnreadByEmployee(
            Long employeeId) {
        return notificationRepository.findByEmployeeIdAndStatus(
                        employeeId, String.valueOf(NotificationStatus.UNREAD))
                .stream().map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getByEmployeeAndType(
            Long employeeId, NotificationType type) {
        return notificationRepository.findByEmployeeId(employeeId)
                .stream()
                .filter(n -> n.getType() == type)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long employeeId) {
        return notificationRepository.countByEmployeeIdAndStatus(
                employeeId, String.valueOf(NotificationStatus.UNREAD));
    }

    // ── DELETE ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new ResourceNotFoundException(
                    "Notification not found: " + notificationId);
        }
        notificationRepository.deleteById(notificationId);
    }

    @Override
    @Transactional
    public void deleteAllByEmployee(Long employeeId) {
        List<Notification> notifications = notificationRepository
                .findByEmployeeId(employeeId);
        if (notifications.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No notifications found for employee: " + employeeId);
        }
        notificationRepository.deleteAll(notifications);
    }

    // ── HELPERS ──────────────────────────────────────────────────────

    private Notification buildNotification(Employee employee,
                                           String title,
                                           String message,
                                           NotificationType type,
                                           User sentBy) {
        Notification n = new Notification();
        n.setEmployee(employee);
        n.setTitle(title);
        n.setMessage(message);
        n.setType(type);
        n.setStatus(NotificationStatus.UNREAD);
        n.setCreatedAt(LocalDateTime.now());
        n.setSentAt(LocalDateTime.now());
        n.setSentBy(sentBy);
        return n;
    }

    // ── MAPPER ───────────────────────────────────────────────────────

    private NotificationResponseDto mapToDto(Notification n) {
        NotificationResponseDto dto = new NotificationResponseDto();
        dto.setId(n.getId());
        dto.setEmployeeId(n.getEmployee().getId());
        dto.setEmployeeName(n.getEmployee().getFirstName()
                + " " + n.getEmployee().getLastName());
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setType(n.getType());
        dto.setStatus(n.getStatus());
        dto.setCreatedAt(n.getCreatedAt());
        dto.setSentAt(n.getSentAt());
        dto.setReadAt(n.getReadAt());
        if (n.getSentBy() != null) {
            dto.setSentByName(n.getSentBy().getUsername());
        }
        return dto;
    }
}