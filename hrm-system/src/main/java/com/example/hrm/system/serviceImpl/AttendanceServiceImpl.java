package com.example.hrm.system.serviceImpl;

import com.example.hrm.system.dtos.requestdto.AttendanceCheckOutDto;
import com.example.hrm.system.dtos.requestdto.AttendanceRequestDto;
import com.example.hrm.system.dtos.responsedto.AttendanceResponseDto;
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
        if (dto == null || dto.getEmployeeId() == null || dto.getDate() == null || dto.getCheckIn() == null) {
            throw new IllegalArgumentException("EmployeeId, Date and CheckIn time are required");
        }

        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + dto.getEmployeeId()));

        if (attendanceRepository.existsByEmployeeIdAndDate(dto.getEmployeeId(), dto.getDate())) {
            throw new IllegalArgumentException("Employee has already checked in today");
        }

        if (dto.getDate().getDayOfWeek() == DayOfWeek.SATURDAY ||
                dto.getDate().getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new IllegalArgumentException("Check-in not allowed on weekends");
        }

        boolean onLeave = leaveRepository.findByEmployeeId(dto.getEmployeeId())
                .stream()
                .anyMatch(l -> l.getStatus() == LeaveStatus.APPROVED &&
                        !dto.getDate().isBefore(l.getStartDate()) &&
                        !dto.getDate().isAfter(l.getEndDate()));

        if (onLeave) {
            throw new IllegalArgumentException("Employee is on approved leave on this date");
        }

        AttendanceStatus status = dto.getCheckIn().isAfter(LATE_THRESHOLD) ?
                AttendanceStatus.LATE : AttendanceStatus.PRESENT;

        if (status == AttendanceStatus.LATE &&
                (dto.getLateReason() == null || dto.getLateReason().trim().isEmpty())) {
            throw new IllegalArgumentException("Late reason is required when checking in late");
        }

        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setDate(dto.getDate());
        attendance.setCheckIn(dto.getCheckIn());
        attendance.setStatus(status);
        attendance.setLateReason(dto.getLateReason());

        Attendance savedAttendance = attendanceRepository.save(attendance);
        return mapToDto(savedAttendance);
    }

    // ================= CHECK-OUT =================
    @Override
    @Transactional
    public AttendanceResponseDto checkOut(AttendanceCheckOutDto dto) {
        if (dto == null || dto.getEmployeeId() == null || dto.getCheckOut() == null) {
            throw new IllegalArgumentException("EmployeeId and CheckOut time are required");
        }

        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndDate(dto.getEmployeeId(), LocalDate.now())
                .orElseThrow(() -> new ResourceNotFoundException("Check-in record not found for today"));

        if (attendance.getCheckOut() != null) {
            throw new IllegalArgumentException("Already checked out today");
        }

        if (dto.getCheckOut().isBefore(attendance.getCheckIn())) {
            throw new IllegalArgumentException("Check-out time cannot be before check-in time");
        }

        double hours = ChronoUnit.MINUTES.between(attendance.getCheckIn(), dto.getCheckOut()) / 60.0;

        // Half day logic
        if (dto.getCheckOut().isBefore(HALF_DAY_CHECKOUT) || hours < (FULL_DAY_HOURS / 2)) {
            attendance.setStatus(AttendanceStatus.HALF_DAY);
        }

        attendance.setCheckOut(dto.getCheckOut());
        attendance.setWorkingHours(Math.round(hours * 100.0) / 100.0);

        Attendance saved = attendanceRepository.save(attendance);
        return mapToDto(saved);
    }

    // ================= GET METHODS =================
    @Override
    @Transactional(readOnly = true)
    public AttendanceResponseDto getAttendanceById(Long id) {
        return mapToDto(attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found")));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponseDto> getAttendanceByEmployee(Long employeeId) {
        return attendanceRepository.findByEmployeeId(employeeId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponseDto> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByDate(date)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponseDto> getAttendanceByEmployeeAndDateRange(
            Long employeeId, LocalDate start, LocalDate end) {
        return attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, start, end)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceReportDto getMonthlyReport(Long employeeId, int month, int year) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Attendance> records = attendanceRepository
                .findByEmployeeIdAndDateBetween(employeeId, start, end);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + employeeId));

        long present = count(records, AttendanceStatus.PRESENT);
        long late = count(records, AttendanceStatus.LATE);
        long absent = count(records, AttendanceStatus.ABSENT);
        long halfDay = count(records, AttendanceStatus.HALF_DAY);

        double totalHours = records.stream()
                .filter(a -> a.getWorkingHours() != null)
                .mapToDouble(Attendance::getWorkingHours)
                .sum();

        long totalWorkingDays = ChronoUnit.DAYS.between(start, end.plusDays(1)) - countWeekends(start, end);

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

        // Attendance percentage = (present + late + half-day credit) / total working days
        double attendedDays = present + late + (halfDay * 0.5);
        double attendancePercentage = totalWorkingDays > 0
                ? Math.round((attendedDays / totalWorkingDays) * 10000.0) / 100.0
                : 0.0;
        report.setAttendancePercentage(attendancePercentage);

        long daysWithRecordedHours = records.stream()
                .filter(a -> a.getWorkingHours() != null)
                .count();
        double avgHoursPerDay = daysWithRecordedHours > 0
                ? Math.round((totalHours / daysWithRecordedHours) * 100.0) / 100.0
                : 0.0;
        report.setAverageWorkingHoursPerDay(avgHoursPerDay);

        return report;
    }

    // ================= AUTO MARK ABSENT =================
    @Scheduled(cron = "0 59 23 * * MON-FRI")
    public void markAbsentees() {
        LocalDate today = LocalDate.now();
        for (Employee emp : employeeRepository.findAll()) {
            if (!attendanceRepository.existsByEmployeeIdAndDate(emp.getId(), today)) {
                Attendance a = new Attendance();
                a.setEmployee(emp);
                a.setDate(today);
                a.setStatus(AttendanceStatus.ABSENT);
                attendanceRepository.save(a);
            }
        }
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