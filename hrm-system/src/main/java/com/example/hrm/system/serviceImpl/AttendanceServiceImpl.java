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

        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (attendanceRepository.existsByEmployeeIdAndDate(dto.getEmployeeId(), dto.getDate())) {
            throw new IllegalArgumentException("Already checked in today");
        }

        if (dto.getDate().getDayOfWeek() == DayOfWeek.SATURDAY ||
                dto.getDate().getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new IllegalArgumentException("Weekend not allowed");
        }

        boolean onLeave = leaveRepository.findByEmployeeId(dto.getEmployeeId())
                .stream()
                .anyMatch(l ->
                        l.getStatus() == LeaveStatus.APPROVED &&
                                !dto.getDate().isBefore(l.getStartDate()) &&
                                !dto.getDate().isAfter(l.getEndDate()));

        if (onLeave) {
            throw new IllegalArgumentException("Employee on leave");
        }

        AttendanceStatus status;
        if (dto.getCheckIn().isAfter(LATE_THRESHOLD)) {
            if (dto.getLateReason() == null || dto.getLateReason().isBlank()) {
                throw new IllegalArgumentException("Late reason required");
            }
            status = AttendanceStatus.LATE;
        } else {
            status = AttendanceStatus.PRESENT;
        }

        Attendance attendance = new Attendance();
        attendance.setEmployee(employee);
        attendance.setDate(dto.getDate());
        attendance.setCheckIn(dto.getCheckIn());
        attendance.setStatus(status);
        attendance.setLateReason(dto.getLateReason());

        return mapToDto(attendanceRepository.save(attendance));
    }

    // ================= CHECK-OUT =================
    @Override
    @Transactional
    public AttendanceResponseDto checkOut(AttendanceCheckOutDto dto) {

        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndDate(dto.getEmployeeId(), LocalDate.now())
                .orElseThrow(() -> new ResourceNotFoundException("Check-in not found"));

        if (attendance.getCheckOut() != null) {
            throw new IllegalArgumentException("Already checked out");
        }

        if (dto.getCheckOut().isBefore(attendance.getCheckIn())) {
            throw new IllegalArgumentException("Invalid checkout time");
        }

        double hours = ChronoUnit.MINUTES.between(
                attendance.getCheckIn(), dto.getCheckOut()) / 60.0;

        if (dto.getCheckOut().isBefore(HALF_DAY_CHECKOUT) ||
                hours < (FULL_DAY_HOURS / 2)) {
            attendance.setStatus(AttendanceStatus.HALF_DAY);
        }

        attendance.setCheckOut(dto.getCheckOut());
        attendance.setWorkingHours(Math.round(hours * 100.0) / 100.0);

        return mapToDto(attendanceRepository.save(attendance));
    }

    // ================= GET =================
    @Override
    public AttendanceResponseDto getAttendanceById(Long id) {
        return mapToDto(attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found")));
    }

    @Override
    public List<AttendanceResponseDto> getAttendanceByEmployee(Long employeeId) {
        return attendanceRepository.findByEmployeeId(employeeId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<AttendanceResponseDto> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByDate(date)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<AttendanceResponseDto> getAttendanceByEmployeeAndDateRange(
            Long employeeId, LocalDate start, LocalDate end) {

        return attendanceRepository
                .findByEmployeeIdAndDateBetween(employeeId, start, end)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    // ================= REPORT =================
    @Override
    public AttendanceReportDto getMonthlyReport(Long employeeId, int month, int year) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<Attendance> records =
                attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, start, end);

        Long present = (long) count(records, AttendanceStatus.PRESENT);
        Long late = (long) count(records, AttendanceStatus.LATE);
        AttendanceReportDto report = new AttendanceReportDto();
        report.setEmployeeId(employeeId);
        report.setMonth(month);
        report.setYear(year);
        report.setPresentDays(present);
        report.setLateDays(late);

        return report;
    }

    // ================= AUTO ABSENT =================
    @Scheduled(cron = "0 59 23 * * MON-FRI")
    public void markAbsentees() {
        LocalDate today = LocalDate.now();

        for (Employee emp : employeeRepository.findAll()) {

            if (attendanceRepository.existsByEmployeeIdAndDate(emp.getId(), today))
                continue;

            Attendance a = new Attendance();
            a.setEmployee(emp);
            a.setDate(today);
            a.setStatus(AttendanceStatus.ABSENT);

            attendanceRepository.save(a);
        }
    }

    // ================= HELPERS =================
    private int count(List<Attendance> list, AttendanceStatus status) {
        return (int) list.stream().filter(a -> a.getStatus() == status).count();
    }

    private AttendanceResponseDto mapToDto(Attendance a) {
        AttendanceResponseDto dto = new AttendanceResponseDto();

        dto.setId(a.getId());
        dto.setEmployeeId(a.getEmployee().getId());
        dto.setEmployeeName(
                a.getEmployee().getFirstName() + " " + a.getEmployee().getLastName());

        dto.setDate(a.getDate());
        dto.setCheckIn(a.getCheckIn());
        dto.setCheckOut(a.getCheckOut());
        dto.setWorkingHours(a.getWorkingHours());
        dto.setStatus(a.getStatus());
        dto.setLateReason(a.getLateReason());

        return dto;
    }
}