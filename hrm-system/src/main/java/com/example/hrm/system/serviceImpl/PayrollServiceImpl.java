package com.example.hrm.system.serviceImpl;

import com.example.hrm.system.dtos.requestdto.PayrollRequestDto;
import com.example.hrm.system.dtos.responsedto.PayrollResponseDto;
import com.example.hrm.system.dtos.updatedto.AttendanceReportDto;
import com.example.hrm.system.dtos.updatedto.PayrollBulkRequestDto;
import com.example.hrm.system.emums.PaymentStatus;
import com.example.hrm.system.entity.Employee;
import com.example.hrm.system.entity.Payroll;
import com.example.hrm.system.exeption.ResourceNotFoundException;
import com.example.hrm.system.repository.EmployeeRepository;
import com.example.hrm.system.repository.PayrollRepository;
import com.example.hrm.system.services.AttendanceService;
import com.example.hrm.system.services.PayrollService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PayrollServiceImpl implements PayrollService {

    private static final double TAX_SLAB_1_LIMIT = 600000;
    private static final double TAX_SLAB_2_LIMIT = 1200000;
    private static final double TAX_SLAB_3_LIMIT = 2400000;
    private static final double TAX_SLAB_4_LIMIT = 3600000;
    private static final double TAX_SLAB_5_LIMIT = 6000000;

    private static final double LATE_DEDUCTION_FACTOR = 0.5;

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceService attendanceService;

    public PayrollServiceImpl(PayrollRepository payrollRepository,
                              EmployeeRepository employeeRepository,
                              AttendanceService attendanceService) {
        this.payrollRepository = payrollRepository;
        this.employeeRepository = employeeRepository;
        this.attendanceService = attendanceService;
    }

    // ================= SINGLE PAYROLL =================
    @Override
    @Transactional
    public PayrollResponseDto generatePayroll(PayrollRequestDto dto) {

        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        if (employee.getDesignation() == null) {
            throw new IllegalArgumentException("Employee has no designation assigned");
        }

        if (payrollRepository.existsByEmployeeIdAndMonthAndYear(
                dto.getEmployeeId(), dto.getMonth(), dto.getYear())) {
            throw new IllegalArgumentException("Payroll already exists for this month");
        }

        AttendanceReportDto report = attendanceService.getMonthlyReport(
                dto.getEmployeeId(), dto.getMonth(), dto.getYear());

        if (report.getTotalWorkingHours() == 0) {
            throw new IllegalArgumentException("No working days in this month");
        }

        double basicSalary = employee.getDesignation().getBaseSalary();
        double perDaySalary = basicSalary / report.getTotalWorkingHours();

        double absentDeduction = perDaySalary * report.getAbsentDays();
        double lateDeduction = perDaySalary * LATE_DEDUCTION_FACTOR * report.getLateDays();
        double halfDayDeduction = perDaySalary * 0.5 * report.getLateDays();

        double extraDeduction = dto.getExtraDeduction() != null ? dto.getExtraDeduction() : 0.0;
        double bonus = dto.getBonus() != null ? dto.getBonus() : 0.0;

        double totalDeduction = absentDeduction + lateDeduction + halfDayDeduction + extraDeduction;

        double annualSalary = basicSalary * 12;
        double monthlyTax = calculateMonthlyTax(annualSalary);

        double netSalary = basicSalary + bonus - totalDeduction - monthlyTax;
        netSalary = Math.max(netSalary, 0.0);

        Payroll payroll = new Payroll();
        payroll.setEmployee(employee);
        payroll.setMonth(dto.getMonth());
        payroll.setYear(dto.getYear());
        payroll.setBasicSalary(round(basicSalary));
        payroll.setPerDaySalary(round(perDaySalary));
        payroll.setPresentDays(Math.toIntExact(report.getPresentDays()));
        payroll.setAbsentDays(Math.toIntExact(report.getAbsentDays()));
        payroll.setLateDays(Math.toIntExact(report.getLateDays()));
        payroll.setBonus(round(bonus));
        payroll.setDeduction(round(totalDeduction));
        payroll.setTax(round(monthlyTax));
        payroll.setNetSalary(round(netSalary));
        payroll.setNotes(dto.getNotes());
        payroll.setPaymentStatus(PaymentStatus.PENDING);
        payroll.setGeneratedDate(LocalDateTime.now());

        return mapToDto(payrollRepository.save(payroll));
    }

    // ================= BULK =================
    @Override
    @Transactional
    public List<PayrollResponseDto> generateBulkPayroll(PayrollBulkRequestDto dto) {

        List<PayrollResponseDto> results = new ArrayList<>();

        for (Employee employee : employeeRepository.findAll()) {
            if (payrollRepository.existsByEmployeeIdAndMonthAndYear(
                    employee.getId(), dto.getMonth(), dto.getYear())) continue;

            try {
                PayrollRequestDto req = new PayrollRequestDto();
                req.setEmployeeId(employee.getId());
                req.setMonth(dto.getMonth());
                req.setYear(dto.getYear());
                req.setBonus(dto.getBonusForAll());

                results.add(generatePayroll(req));
            } catch (Exception ignored) {}
        }

        return results;
    }

    // ================= MARK PAID =================
    @Override
    @Transactional
    public PayrollResponseDto markAsPaid(Long id) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll not found"));

        if (payroll.getPaymentStatus() == PaymentStatus.PAID)
            throw new IllegalArgumentException("Already paid");

        payroll.setPaymentStatus(PaymentStatus.PAID);
        payroll.setPaidDate(LocalDateTime.now());

        return mapToDto(payrollRepository.save(payroll));
    }

    // ================= CANCEL =================
    @Override
    @Transactional
    public PayrollResponseDto cancelPayroll(Long id) {
        Payroll payroll = payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll not found"));

        if (payroll.getPaymentStatus() == PaymentStatus.PAID)
            throw new IllegalArgumentException("Cannot cancel paid payroll");

        payroll.setPaymentStatus(PaymentStatus.CANCELLED);
        return mapToDto(payrollRepository.save(payroll));
    }

    // ================= GET =================
    @Override
    public PayrollResponseDto getPayrollById(Long id) {
        return mapToDto(payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Not found")));
    }

    @Override
    public List<PayrollResponseDto> getPayrollByEmployee(Long employeeId) {
        return payrollRepository.findByEmployeeId(employeeId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<PayrollResponseDto> getPayrollByMonthAndYear(int month, int year) {
        return payrollRepository.findByMonthAndYear(month, year)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<PayrollResponseDto> getUnpaidPayrolls() {
        return payrollRepository.findByPaymentStatus(String.valueOf(PaymentStatus.PENDING))
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public Double getTotalPayrollCostByMonthAndYear(int month, int year) {
        return payrollRepository.findByMonthAndYear(month, year)
                .stream()
                .filter(p -> p.getPaymentStatus() != PaymentStatus.CANCELLED)
                .mapToDouble(Payroll::getNetSalary)
                .sum();
    }

    // ================= TAX =================
    private double calculateMonthlyTax(double annualSalary) {
        double tax = 0;

        if (annualSalary > TAX_SLAB_1_LIMIT)
            tax += Math.min(annualSalary, TAX_SLAB_2_LIMIT) - TAX_SLAB_1_LIMIT * 0.05;

        return tax / 12;
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    // ================= DTO =================
    private PayrollResponseDto mapToDto(Payroll p) {
        PayrollResponseDto dto = new PayrollResponseDto();

        dto.setId(p.getId());
        dto.setEmployeeId(p.getEmployee().getId());
        dto.setEmployeeName(
                p.getEmployee().getFirstName() + " " + p.getEmployee().getLastName());

        dto.setDepartment(
                p.getEmployee().getDepartment() != null
                        ? p.getEmployee().getDepartment().getName()
                        : null
        );

        dto.setDesignation(
                p.getEmployee().getDesignation() != null
                        ? p.getEmployee().getDesignation().getTitle()
                        : null
        );

        dto.setMonth(p.getMonth());
        dto.setYear(p.getYear());
        dto.setBasicSalary(p.getBasicSalary());
        dto.setPerDaySalary(p.getPerDaySalary());
        dto.setPresentDays(p.getPresentDays());
        dto.setAbsentDays(p.getAbsentDays());
        dto.setLateDays(p.getLateDays());
        dto.setBonus(p.getBonus());
        dto.setDeduction(p.getDeduction());
        dto.setTax(p.getTax());
        dto.setNetSalary(p.getNetSalary());
        dto.setNotes(p.getNotes());
        dto.setPaymentStatus(p.getPaymentStatus());
        dto.setGeneratedDate(p.getGeneratedDate());
        dto.setPaidDate(p.getPaidDate());

        return dto;
    }
}