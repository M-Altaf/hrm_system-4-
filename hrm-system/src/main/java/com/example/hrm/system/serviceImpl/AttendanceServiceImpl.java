package com.example.hrm.system.serviceImpl;

import com.example.hrm.system.dtos.requestdto.AttendanceCheckOutDto;
import com.example.hrm.system.dtos.requestdto.AttendanceRequestDto;
import com.example.hrm.system.dtos.responsedto.AttendanceResponseDto;
import com.example.hrm.system.dtos.updatedto.AttendanceReportDto;
import com.example.hrm.system.emums.AttendanceStatus;
import com.example.hrm.system.emums.LeaveStatus;
import com.example.hrm.system.entity.Attendance;
import com.example.hrm.system.entity.Employee;
import com.example.hrm.system.exception.ResourceNotFoundException;
import com.example.hrm.system.repository.AttendanceRepository;
import com.example.hrm.system.repository.EmployeeRepository;
import com.example.hrm.system.repository.LeaveRepository;
import com.example.hrm.system.services.AttendanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AttendanceServiceImpl implements AttendanceService {

    private static final LocalTime LATE_THRESHOLD = LocalTime.of(9, 30);
    private static final LocalTime HALF_DAY_CHECKOUT = LocalTime.of(13, 0);
    private static final double FULL_DAY_HOURS = 8.0;

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

    // ================= CHECK-IN =================
    @Override
    @Transactional
    public AttendanceResponseDto checkIn(AttendanceRequestDto dto) {
        // Validate input
        if (dto == null) {
            log.warn("Attendance request DTO is null");
            throw new IllegalArgumentException("Request body cannot be null");
        }

        if (dto.getEmployeeId() == null) {
            log.warn("Employee ID is null in check-in request");
            throw new IllegalArgumentException("Employee ID is required");
        }

        if (dto.getDate() == null) {
            log.warn("Date is null in check-in request");
            throw new IllegalArgumentException("Date is required");
        }

        if (dto.getCheckIn() == null) {
            log.warn("Check-in time is null in request");
            throw new IllegalArgumentException("Check-in time is required");
        }

        Long employeeId = dto.getEmployeeId();
        LocalDate date = dto.getDate();
        LocalTime checkInTime = dto.getCheckIn();

        log.info("Processing check-in for employeeId={}, date={}, checkInTime={}",
                employeeId, date, checkInTime);

        // 1. Verify employee exists
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> {
                    log.error("Employee not found with id: {}", employeeId);
                    return new ResourceNotFoundException("Employee not found with id: " + employeeId);
                });

        log.debug("Employee found: {} {}", employee.getFirstName(), employee.getLastName());

        // 2. Check if already checked in today
        if (attendanceRepository.existsByEmployeeIdAndDate(employeeId, date)) {
            log.warn("Employee {} already checked in on {}", employeeId, date);
            throw new IllegalArgumentException("Employee has already checked in on this date");
        }

        // 3. Check for weekends
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            log.warn("Attempted check-in on weekend: {}", date);
            throw new IllegalArgumentException("Check-in not allowed on weekends");
        }

        // 4. Check if employee is on approved leave
        boolean onLeave = leaveRepository.findByEmployeeId(employeeId)
                .stream()
                .anyMatch(l -> l.getStatus() == LeaveStatus.APPROVED &&
                        !date.isBefore(l.getStartDate()) &&
                        !date.isAfter(l.getEndDate()));

        if (onLeave) {
            log.warn("Employee {} is on approved leave on {}", employeeId, date);
            throw new IllegalArgumentException("Employee is on approved leave on this date");
        }

        // 5. Determine attendance status (PRESENT or LATE)
        AttendanceStatus status = checkInTime.isAfter(LATE_THRESHOLD) ?
                AttendanceStatus.LATE : AttendanceStatus.PRESENT;

        log.debug("Attendance status determined: {}", status);

        // 6. If LATE, late reason is mandatory
        if (status == AttendanceStatus.LATE) {
            if (dto.getLateReason() == null || dto.getLateReason().trim().isEmpty()) {
                log.warn("Late check-in without reason for employeeId={}", employeeId);
                throw new IllegalArgumentException("Late reason is required when checking in after 9:30 AM");
            }
            log.debug("Late reason provided: {}", dto.getLateReason());
        }

        // 7. Create and save attendance record
        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setDate(date);
        attendance.setCheckIn(checkInTime);
        attendance.setStatus(status);
        attendance.setLateReason(dto.getLateReason());

        Attendance savedAttendance = attendanceRepository.save(attendance);
        log.info("Check-in successful for employeeId={}, attendanceId={}, status={}",
                employeeId, savedAttendance.getId(), status);

        return mapToDto(savedAttendance);
    }

    // ================= CHECK-OUT =================
    @Override
    @Transactional
    public AttendanceResponseDto checkOut(AttendanceCheckOutDto dto) {
        // Validate input
        if (dto == null) {
            log.warn("Attendance check-out request DTO is null");
            throw new IllegalArgumentException("Request body cannot be null");
        }

        if (dto.getEmployeeId() == null) {
            log.warn("Employee ID is null in check-out request");
            throw new IllegalArgumentException("Employee ID is required");
        }

        if (dto.getCheckOut() == null) {
            log.warn("Check-out time is null in request");
            throw new IllegalArgumentException("Check-out time is required");
        }

        Long employeeId = dto.getEmployeeId();
        LocalTime checkOutTime = dto.getCheckOut();
        LocalDate today = LocalDate.now();

        log.info("Processing check-out for employeeId={}, checkOutTime={}", employeeId, checkOutTime);

        // 1. Find today's check-in record
        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndDate(employeeId, today)
                .orElseThrow(() -> {
                    log.error("Check-in record not found for employeeId={} on {}", employeeId, today);
                    return new ResourceNotFoundException(
                            "No check-in record found for today. Please check-in first.");
                });

        log.debug("Check-in record found: {}", attendance.getId());

        // 2. Verify not already checked out
        if (attendance.getCheckOut() != null) {
            log.warn("Employee {} already checked out on {}", employeeId, today);
            throw new IllegalArgumentException("You have already checked out today");
        }

        // 3. Validate check-out time is after check-in
        if (checkOutTime.isBefore(attendance.getCheckIn())) {
            log.warn("Check-out time {} is before check-in time {} for employeeId={}",
                    checkOutTime, attendance.getCheckIn(), employeeId);
            throw new IllegalArgumentException("Check-out time cannot be before check-in time");
        }

        // 4. Calculate working hours
        double hours = ChronoUnit.MINUTES.between(attendance.getCheckIn(), checkOutTime) / 60.0;
        log.debug("Working hours calculated: {}", hours);

        // 5. Determine if half-day
        if (checkOutTime.isBefore(HALF_DAY_CHECKOUT) || hours < (FULL_DAY_HOURS / 2)) {
            attendance.setStatus(AttendanceStatus.HALF_DAY);
            log.debug("Status set to HALF_DAY");
        }

        // 6. Update check-out and working hours
        attendance.setCheckOut(checkOutTime);
        attendance.setWorkingHours(Math.round(hours * 100.0) / 100.0);

        Attendance saved = attendanceRepository.save(attendance);
        log.info("Check-out successful for employeeId={}, attendanceId={}, workingHours={}",
                employeeId, saved.getId(), saved.getWorkingHours());

        return mapToDto(saved);
    }

    // ================= GET METHODS =================
    @Override
    @Transactional(readOnly = true)
    public AttendanceResponseDto getAttendanceById(Long id) {
        log.debug("Fetching attendance record by id={}", id);
        return mapToDto(attendanceRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Attendance record not found with id={}", id);
                    return new ResourceNotFoundException("Attendance record not found");
                }));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponseDto> getAttendanceByEmployee(Long employeeId) {
        log.debug("Fetching attendance records for employeeId={}", employeeId);
        List<AttendanceResponseDto> result = attendanceRepository.findByEmployeeId(employeeId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
        log.debug("Found {} records for employeeId={}", result.size(), employeeId);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponseDto> getAttendanceByDate(LocalDate date) {
        log.debug("Fetching attendance records for date={}", date);
        List<AttendanceResponseDto> result = attendanceRepository.findByDate(date)
                .stream().map(this::mapToDto).collect(Collectors.toList());
        log.debug("Found {} records for date={}", result.size(), date);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponseDto> getAttendanceByEmployeeAndDateRange(
            Long employeeId, LocalDate start, LocalDate end) {
        log.debug("Fetching attendance records for employeeId={} between {} and {}",
                employeeId, start, end);
        List<AttendanceResponseDto> result = attendanceRepository
                .findByEmployeeIdAndDateBetween(employeeId, start, end)
                .stream().map(this::mapToDto).collect(Collectors.toList());
        log.debug("Found {} records for range", result.size());
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceReportDto getMonthlyReport(Long employeeId, int month, int year) {
        log.info("Generating monthly report for employeeId={}, month={}, year={}",
                employeeId, month, year);

        // Validate month and year
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        if (year < 2000 || year > 2100) {
            throw new IllegalArgumentException("Year must be between 2000 and 2100");
        }

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        log.debug("Report period: {} to {}", start, end);

        List<Attendance> records = attendanceRepository
                .findByEmployeeIdAndDateBetween(employeeId, start, end);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        // Count attendance statuses
        long present = count(records, AttendanceStatus.PRESENT);
        long late = count(records, AttendanceStatus.LATE);
        long absent = count(records, AttendanceStatus.ABSENT);
        long halfDay = count(records, AttendanceStatus.HALF_DAY);

        // Calculate total hours
        double totalHours = records.stream()
                .filter(a -> a.getWorkingHours() != null)
                .mapToDouble(Attendance::getWorkingHours)
                .sum();

        // Count working days (excluding weekends)
        long totalWorkingDays = ChronoUnit.DAYS.between(start, end.plusDays(1)) - countWeekends(start, end);

        // Build report
        AttendanceReportDto report = new AttendanceReportDto();
        report.setEmployeeId(employeeId);
        report.setEmployeeName(employee.getFirstName() + " " + employee.getLastName());
        report.setMonth(month);
        report.setYear(year);
        report.setTotalDays(totalWorkingDays);
        report.setPresentDays(present);
        report.setLateDays(late);
        report.setAbsentDays(absent);
        report.setHalfDayCount(halfDay);
        report.setTotalWorkingHours(Math.round(totalHours * 100.0) / 100.0);

        // Calculate attendance percentage
        // Formula: (present + late + half-day*0.5) / total working days * 100
        double attendedDays = present + late + (halfDay * 0.5);
        double attendancePercentage = totalWorkingDays > 0
                ? Math.round((attendedDays / totalWorkingDays) * 10000.0) / 100.0
                : 0.0;
        report.setAttendancePercentage(attendancePercentage);

        // Calculate average hours per day
        long daysWithRecordedHours = records.stream()
                .filter(a -> a.getWorkingHours() != null)
                .count();
        double avgHoursPerDay = daysWithRecordedHours > 0
                ? Math.round((totalHours / daysWithRecordedHours) * 100.0) / 100.0
                : 0.0;
        report.setAverageWorkingHoursPerDay(avgHoursPerDay);

        log.info("Monthly report generated: present={}, late={}, absent={}, halfDay={}, attendance%={}",
                present, late, absent, halfDay, attendancePercentage);

        return report;
    }

    // ================= AUTO MARK ABSENT (Scheduled Task) =================
    @Scheduled(cron = "0 59 23 * * MON-FRI")
    @Transactional
    public void markAbsentees() {
        LocalDate today = LocalDate.now();
        log.info("Starting scheduled task to mark absentees for {}", today);

        // Skip if it's a weekend
        if (today.getDayOfWeek() == DayOfWeek.SATURDAY || today.getDayOfWeek() == DayOfWeek.SUNDAY) {
            log.debug("Skipping - today is weekend");
            return;
        }

        int count = 0;
        for (Employee emp : employeeRepository.findAll()) {
            // Skip employees on leave
            boolean onLeave = leaveRepository.findByEmployeeId(emp.getId())
                    .stream()
                    .anyMatch(l -> l.getStatus() == LeaveStatus.APPROVED &&
                            !today.isBefore(l.getStartDate()) &&
                            !today.isAfter(l.getEndDate()));

            if (onLeave) {
                log.debug("Skipping employee {} - on approved leave", emp.getId());
                continue;
            }

            // Mark absent if no attendance record
            if (!attendanceRepository.existsByEmployeeIdAndDate(emp.getId(), today)) {
                Attendance a = new Attendance();
                a.setEmployee(emp);
                a.setDate(today);
                a.setStatus(AttendanceStatus.ABSENT);
                attendanceRepository.save(a);
                count++;
                log.debug("Marked employee {} as absent", emp.getId());
            }
        }

        log.info("Marked {} employees as absent for {}", count, today);
    }

    // ================= HELPER METHODS =================
    private int count(List<Attendance> list, AttendanceStatus status) {
        return (int) list.stream().filter(a -> a.getStatus() == status).count();
    }

    private long countWeekends(LocalDate start, LocalDate end) {
        return start.datesUntil(end.plusDays(1))
                .filter(date -> date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                        date.getDayOfWeek() == DayOfWeek.SUNDAY)
                .count();
    }

    private AttendanceResponseDto mapToDto(Attendance a) {
        AttendanceResponseDto dto = new AttendanceResponseDto();
        dto.setId(a.getId());
        dto.setEmployeeId(a.getEmployee().getId());
        dto.setEmployeeName(a.getEmployee().getFirstName() + " " + a.getEmployee().getLastName());
        dto.setDate(a.getDate());
        dto.setCheckIn(a.getCheckIn());
        dto.setCheckOut(a.getCheckOut());
        dto.setWorkingHours(a.getWorkingHours());
        dto.setStatus(a.getStatus());
        dto.setLateReason(a.getLateReason());
        return dto;
    }
}
