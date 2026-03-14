package com.example.hrm.system.serviceImpl;



import com.example.hrm.system.dtos.requestdto.AttendanceRequestDto;
import com.example.hrm.system.dtos.responsedto.AttendanceResponseDto;
import com.example.hrm.system.dtos.updatedto.AttendanceCheckOutDto;
import com.example.hrm.system.dtos.updatedto.AttendanceReportDto;
import com.example.hrm.system.emums.AttendanceStatus;
import com.example.hrm.system.emums.LeaveStatus;
import com.example.hrm.system.entity.Attendance;
import com.example.hrm.system.entity.Employee;
import com.example.hrm.system.exeption.ResourceNotFoundException;
import com.example.hrm.system.repository.AttendanceRepository;
import com.example.hrm.system.repository.EmployeeRepository;
import com.example.hrm.system.repository.LeaveRepository;
import com.example.hrm.system.services.AttendanceService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttendanceServiceImpl implements AttendanceService {

    // Office rules
    private static final LocalTime OFFICE_START     = LocalTime.of(9, 0);   // 9:00 AM
    private static final LocalTime LATE_THRESHOLD   = LocalTime.of(9, 30);  // 9:30 AM = LATE
    private static final LocalTime HALF_DAY_CHECKOUT = LocalTime.of(13, 0); // before 1:00 PM = HALF DAY
    private static final double    FULL_DAY_HOURS   = 8.0;                  // 8 hours = full day

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveRepository leaveRepository;

    public AttendanceServiceImpl(AttendanceRepository attendanceRepository,
                                 EmployeeRepository employeeRepository,
                                 LeaveRepository leaveRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.leaveRepository = leaveRepository;
    }

    // ── CHECK IN ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public AttendanceResponseDto checkIn(AttendanceRequestDto dto) {

        // 1. Check employee exists
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found: " + dto.getEmployeeId()));

        // 2. Prevent duplicate check-in on same day
        if (attendanceRepository.existsByEmployeeIdAndDate(
                dto.getEmployeeId(), dto.getDate())) {
            throw new IllegalArgumentException(
                    "Employee has already checked in today: " + dto.getDate());
        }

        // 3. Cannot check in on weekends
        DayOfWeek day = dto.getDate().getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            throw new IllegalArgumentException(
                    "Cannot mark attendance on weekends");
        }

        // 4. Check if employee is on approved leave today
        boolean onLeave = leaveRepository
                .findByEmployeeId(dto.getEmployeeId())
                .stream()
                .anyMatch(l -> l.getStatus().name().equals(LeaveStatus.APPROVED.name())
                        && !dto.getDate().isBefore(l.getStartDate())
                        && !dto.getDate().isAfter(l.getEndDate()));

        if (onLeave) {
            throw new IllegalArgumentException(
                    "Employee is on approved leave today — cannot check in");
        }

        // 5. Determine status based on check-in time
        LocalTime checkInTime = dto.getCheckIn();
        AttendanceStatus status;

        if (checkInTime.isAfter(LATE_THRESHOLD)) {
            // Late arrival — lateReason is required
            if (dto.getLateReason() == null || dto.getLateReason().isBlank()) {
                throw new IllegalArgumentException(
                        "Late reason is required when checking in after "
                                + LATE_THRESHOLD);
            }
            status = AttendanceStatus.LATE;
        } else {
            status = AttendanceStatus.PRESENT;
        }

        // 6. Save attendance
        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setDate(dto.getDate());
        attendance.setCheckIn(checkInTime);
        attendance.setStatus(status);
        attendance.setLateReason(dto.getLateReason());

        return mapToDto(attendanceRepository.save(attendance));
    }

    // ── CHECK OUT ────────────────────────────────────────────────────

    @Override
    @Transactional
    public AttendanceResponseDto checkOut(Long attendanceId,
                                          AttendanceCheckOutDto dto) {

        // 1. Find today's attendance record
        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndDate(dto.getEmployeeId(), LocalDate.now())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No check-in found for today. Please check in first"));

        // 2. Prevent duplicate check-out
        if (attendance.getCheckOut() != null) {
            throw new IllegalArgumentException(
                    "Employee has already checked out today");
        }

        // 3. Checkout must be after check-in
        if (dto.getCheckOut().isBefore(attendance.getCheckIn())) {
            throw new IllegalArgumentException(
                    "Check-out time cannot be before check-in time");
        }

        // 4. Calculate working hours
        double workingHours = ChronoUnit.MINUTES.between(
                attendance.getCheckIn(), dto.getCheckOut()) / 60.0;

        // 5. Update status based on working hours
        if (dto.getCheckOut().isBefore(HALF_DAY_CHECKOUT)) {
            attendance.setStatus(AttendanceStatus.HALF_DAY);
        } else if (workingHours < FULL_DAY_HOURS / 2
                && attendance.getStatus() != AttendanceStatus.LATE) {
            attendance.setStatus(AttendanceStatus.HALF_DAY);
        }

        attendance.setCheckOut(dto.getCheckOut());
        attendance.setWorkingHours(
                Math.round(workingHours * 100.0) / 100.0);  // round to 2 decimals

        return mapToDto(attendanceRepository.save(attendance));
    }

    // ── GET METHODS ──────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponseDto getAttendanceById(Long id) {
        return mapToDto(attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Attendance not found: " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponseDto> getAttendanceByEmployee(
            Long employeeId) {
        return attendanceRepository.findByEmployeeId(employeeId)
                .stream().map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponseDto> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByDate(date)
                .stream().map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponseDto> getAttendanceByEmployeeAndDateRange(
            Long employeeId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository
                .findByEmployeeIdAndDateBetween(employeeId, startDate, endDate)
                .stream().map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ── MONTHLY REPORT ───────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public AttendanceReportDto getMonthlyReport(Long employeeId,
                                                int month, int year) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found: " + employeeId));

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate   = startDate.withDayOfMonth(
                startDate.lengthOfMonth());

        List<Attendance> records = attendanceRepository
                .findByEmployeeIdAndDateBetween(employeeId, startDate, endDate);

        // Count working days in month (exclude weekends)
        int totalWorkingDays = 0;
        LocalDate date = startDate;
        while (!date.isAfter(endDate)) {
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY
                    && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                totalWorkingDays++;
            }
            date = date.plusDays(1);
        }

        // Count each status
        int presentDays  = (int) records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        int lateDays     = (int) records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LATE).count();
        int halfDays     = (int) records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.HALF_DAY).count();
        int onLeaveDays  = (int) records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ON_LEAVE).count();
        int absentDays   = totalWorkingDays
                - presentDays - lateDays - halfDays - onLeaveDays;

        double totalHours = records.stream()
                .filter(a -> a.getWorkingHours() != null)
                .mapToDouble(Attendance::getWorkingHours).sum();

        double attendancePct = totalWorkingDays > 0
                ? Math.round(((presentDays + lateDays + halfDays)
                / (double) totalWorkingDays) * 100.0)
                : 0.0;

        // Build report
        AttendanceReportDto report = new AttendanceReportDto();
        report.setEmployeeId(employeeId);
        report.setEmployeeName(employee.getFirstName()
                + " " + employee.getLastName());
        report.setMonth(month);
        report.setYear(year);
        report.setTotalWorkingDays(totalWorkingDays);
        report.setPresentDays(presentDays);
        report.setAbsentDays(Math.max(absentDays, 0));
        report.setLateDays(lateDays);
        report.setHalfDays(halfDays);
        report.setOnLeaveDays(onLeaveDays);
        report.setTotalWorkingHours(
                Math.round(totalHours * 100.0) / 100.0);
        report.setAttendancePercentage(attendancePct);

        return report;
    }

    // ── AUTO MARK ABSENT ─────────────────────────────────────────────
    // Runs every day at 11:59 PM automatically

    @Override
    @Scheduled(cron = "0 59 23 * * MON-FRI")
    @Transactional
    public void markAbsentees(LocalDate date) {
        LocalDate today = LocalDate.now();
        List<Employee> allEmployees = employeeRepository.findAll();

        for (Employee employee : allEmployees) {
            // Skip if already has attendance today
            if (attendanceRepository.existsByEmployeeIdAndDate(
                    employee.getId(), today)) {
                continue;
            }

            // Skip if on approved leave
            boolean onLeave = leaveRepository
                    .findByEmployeeId(employee.getId())
                    .stream()
                    .anyMatch(l -> l.getStatus().name()
                            .equals(LeaveStatus.APPROVED.name())
                            && !today.isBefore(l.getStartDate())
                            && !today.isAfter(l.getEndDate()));

            // Mark as ON_LEAVE or ABSENT
            Attendance attendance = new Attendance();
            attendance.setEmployee(employee);
            attendance.setDate(today);
            attendance.setStatus(onLeave
                    ? AttendanceStatus.ON_LEAVE
                    : AttendanceStatus.ABSENT);
            attendanceRepository.save(attendance);
        }
    }

    // ── MAPPER ───────────────────────────────────────────────────────

    private AttendanceResponseDto mapToDto(Attendance a) {
        AttendanceResponseDto dto = new AttendanceResponseDto();
        dto.setId(a.getId());
        dto.setEmployeeId(a.getEmployee().getId());
        dto.setEmployeeName(a.getEmployee().getFirstName()
                + " " + a.getEmployee().getLastName());
        dto.setDate(a.getDate());
        dto.setCheckIn(a.getCheckIn());
        dto.setCheckOut(a.getCheckOut());
        dto.setWorkingHours(a.getWorkingHours());
        dto.setStatus(a.getStatus());
        dto.setLateReason(a.getLateReason());
        return dto;
    }
}
