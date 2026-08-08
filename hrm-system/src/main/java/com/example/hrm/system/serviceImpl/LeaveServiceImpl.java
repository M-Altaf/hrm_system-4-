package com.example.hrm.system.serviceImpl;




import com.example.hrm.system.dtos.requestdto.LeaveRequestDto;


import com.example.hrm.system.dtos.responsedto.LeaveResponseDto;
import com.example.hrm.system.dtos.updatedto.LeaveStatusUpdateDto;
import com.example.hrm.system.emums.LeaveStatus;
import com.example.hrm.system.entity.Employee;
import com.example.hrm.system.entity.Leave;
import com.example.hrm.system.entity.User;
import com.example.hrm.system.exception.ResourceNotFoundException;
import com.example.hrm.system.repository.EmployeeRepository;
import com.example.hrm.system.repository.LeaveRepository;
import com.example.hrm.system.repository.UserRepository;
import com.example.hrm.system.services.LeaveService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRepository leaveRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    public LeaveServiceImpl(LeaveRepository leaveRepository,
                            EmployeeRepository employeeRepository,
                            UserRepository userRepository) {
        this.leaveRepository = leaveRepository;
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }

    // ── APPLY LEAVE ──────────────────────────────────────────────────

    @Override
    @Transactional
    public LeaveResponseDto applyLeave(LeaveRequestDto dto) {

        // 1. Check employee exists
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found: " + dto.getEmployeeId()));

        // 2. Validate dates
        if (dto.getStartDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Start date cannot be in the past");
        }

        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new IllegalArgumentException(
                    "End date cannot be before start date");
        }

        // 3. Calculate working days (exclude weekends)
        int totalDays = calculateWorkingDays(dto.getStartDate(), dto.getEndDate());

        if (totalDays == 0) {
            throw new IllegalArgumentException(
                    "Leave period contains no working days");
        }

        // 4. Check for overlapping leaves
        List<Leave> overlapping = leaveRepository
                .findByEmployeeId(dto.getEmployeeId())
                .stream()
                .filter(l -> l.getStatus() != LeaveStatus.REJECTED
                        && l.getStatus() != LeaveStatus.CANCELLED)
                .filter(l -> !dto.getStartDate().isAfter(l.getEndDate())
                        && !dto.getEndDate().isBefore(l.getStartDate()))
                .collect(Collectors.toList());

        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException(
                    "You already have a leave request for these dates");
        }

        // 5. Check leave balance (max 20 days per year per type)
        long usedDays = leaveRepository.countByEmployeeIdAndLeaveType(
                dto.getEmployeeId(),
                dto.getLeaveType());

        int maxDays = getMaxDaysForLeaveType(String.valueOf(dto.getLeaveType()));

        if (usedDays + totalDays > maxDays) {
            throw new IllegalArgumentException(
                    "Insufficient leave balance. Available: "
                            + (maxDays - usedDays) + " days");
        }

        // 6. Save leave
        Leave leave = new Leave();
        leave.setEmployee(employee);
        leave.setLeaveType(dto.getLeaveType());
        leave.setStartDate(dto.getStartDate());
        leave.setEndDate(dto.getEndDate());
        leave.setTotalDays(totalDays);
        leave.setReason(dto.getReason());
        leave.setStatus(LeaveStatus.PENDING);
        leave.setAppliedDate(LocalDateTime.now());

        return mapToDto(leaveRepository.save(leave));
    }

    // ── APPROVE / REJECT ─────────────────────────────────────────────

    @Override
    @Transactional
    public LeaveResponseDto updateLeaveStatus(Long leaveId,
                                              LeaveStatusUpdateDto dto) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Leave not found: " + leaveId));

        // Can only update PENDING leaves
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only PENDING leaves can be approved or rejected. "
                            + "Current status: " + leave.getStatus());
        }

        // Rejection must have a reason
        if (dto.getStatus() == LeaveStatus.REJECTED
                && (dto.getRejectionReason() == null
                || dto.getRejectionReason().isBlank())) {
            throw new IllegalArgumentException(
                    "Rejection reason is required when rejecting a leave");
        }

        // Set approver
        if (dto.getApprovedById() != null) {
            User approver = userRepository.findById(dto.getApprovedById())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Approver not found: " + dto.getApprovedById()));
            leave.setApprovedBy(approver);
        }

        leave.setStatus(dto.getStatus());
        leave.setRejectionReason(dto.getRejectionReason());
        leave.setUpdatedAt(LocalDateTime.now());

        return mapToDto(leaveRepository.save(leave));
    }

    // ── CANCEL LEAVE ─────────────────────────────────────────────────

    @Override
    @Transactional
    public LeaveResponseDto cancelLeave(Long leaveId, Long employeeId) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Leave not found: " + leaveId));

        // Only owner can cancel
        if (!leave.getEmployee().getId().equals(employeeId)) {
            throw new IllegalArgumentException(
                    "You can only cancel your own leave requests");
        }

        // Can only cancel PENDING leaves
        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only PENDING leaves can be cancelled. "
                            + "Current status: " + leave.getStatus());
        }

        leave.setStatus(LeaveStatus.CANCELLED);
        leave.setUpdatedAt(LocalDateTime.now());

        return mapToDto(leaveRepository.save(leave));
    }

    // ── GET METHODS ──────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponseDto> getLeavesByEmployee(Long employeeId) {
        return leaveRepository.findByEmployeeId(employeeId)
                .stream().map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
   public List<LeaveResponseDto> getPendingLeaves() {
        return leaveRepository.findByStatus(LeaveStatus.PENDING)
                .stream().map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponseDto> getAllLeaves() {
        return leaveRepository.findAll()
                .stream().map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveResponseDto getLeaveById(Long id) {
        return mapToDto(leaveRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Leave not found: " + id)));
    }

    // ── HELPERS ──────────────────────────────────────────────────────

    // Calculate working days excluding weekends
    private int calculateWorkingDays(LocalDate start, LocalDate end) {
        int workingDays = 0;
        LocalDate date = start;
        while (!date.isAfter(end)) {
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY
                    && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                workingDays++;
            }
            date = date.plusDays(1);
        }
        return workingDays;
    }

    // Max allowed days per leave type per year
    private int getMaxDaysForLeaveType(String leaveType) {
        return switch (leaveType) {
            case "ANNUAL"    -> 20;
            case "SICK"      -> 15;
            case "CASUAL"    -> 10;
            case "MATERNITY" -> 90;
            case "PATERNITY" -> 14;
            case "UNPAID"    -> 30;
            default          -> 10;
        };
    }

    // ── MAPPER ───────────────────────────────────────────────────────

    private LeaveResponseDto mapToDto(Leave l) {
        LeaveResponseDto dto = new LeaveResponseDto();
        dto.setId(l.getId());
        dto.setEmployeeId(l.getEmployee().getId());
        dto.setEmployeeName(l.getEmployee().getFirstName()
                + " " + l.getEmployee().getLastName());
        dto.setLeaveType(l.getLeaveType());
        dto.setStartDate(l.getStartDate());
        dto.setEndDate(l.getEndDate());
        dto.setTotalDays(l.getTotalDays());
        dto.setReason(l.getReason());
        dto.setStatus(l.getStatus());
        dto.setAppliedDate(l.getAppliedDate());
        dto.setRejectionReason(l.getRejectionReason());
        if (l.getApprovedBy() != null) {
            dto.setApprovedByName(l.getApprovedBy().getUsername());
        }
        return dto;
    }
}