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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PayrollServiceImpl implements PayrollService {

    // Tax slabs (annual salary based)
    private static final double TAX_SLAB_1_LIMIT  = 600000;   // 0% up to 600k/year
    private static final double TAX_SLAB_2_LIMIT  = 1200000;  // 5% up to 1.2M/year
    private static final double TAX_SLAB_3_LIMIT  = 2400000;  // 10% up to 2.4M/year
    private static final double TAX_SLAB_4_LIMIT  = 3600000;  // 15% up to 3.6M/year
    private static final double TAX_SLAB_5_LIMIT  = 6000000;  // 20% up to 6M/year
    // 25% above 6M/year
    // Late deduction — deduct 0.5 day salary per late arrival
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

    // ── GENERATE SINGLE PAYROLL ──────────────────────────────────────

    @Override
    @Transactional
    public PayrollResponseDto generatePayroll(PayrollRequestDto dto) {

        // 1. Check employee exists
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found: " + dto.getEmployeeId()));

        // 2. Prevent duplicate payroll for same month/year
        if (payrollRepository.existsByEmployeeIdAndMonthAndYear(
                dto.getEmployeeId(), dto.getMonth(), dto.getYear())) {
            throw new IllegalArgumentException(
                    "Payroll already generated for employee "
                            + employee.getFirstName()
                            + " for " + dto.getMonth() + "/" + dto.getYear());
        }

        // 3. Get attendance report for the month
        AttendanceReportDto report = attendanceService.getMonthlyReport(
                dto.getEmployeeId(), dto.getMonth(), dto.getYear());

        // 4. Get basic salary from designation
        double basicSalary = employee.getDesignation().getBaseSalary();

        // 5. Calculate per day salary
        double perDaySalary = basicSalary / report.getTotalWorkingDays();

        // 6. Calculate absent deduction
        double absentDeduction = perDaySalary * report.getAbsentDays();

        // 7. Calculate late deduction (0.5 day per late)
        double lateDeduction = perDaySalary
                * LATE_DEDUCTION_FACTOR * report.getLateDays();

        // 8. Half day deduction
        double halfDayDeduction = perDaySalary
                * 0.5 * report.getHalfDays();

        // 9. Total deduction
        double totalDeduction = absentDeduction
                + lateDeduction
                + halfDayDeduction
                + (dto.getExtraDeduction() != null ? dto.getExtraDeduction() : 0.0);

        // 10. Bonus
        double bonus = dto.getBonus() != null ? dto.getBonus() : 0.0;

        // 11. Calculate tax
        double annualSalary = basicSalary * 12;
        double monthlyTax   = calculateMonthlyTax(annualSalary);

        // 12. Calculate net salary
        double netSalary = basicSalary + bonus - totalDeduction - monthlyTax;
        netSalary = Math.max(netSalary, 0.0);  // net salary cannot be negative

        // 13. Save payroll
        Payroll payroll = new Payroll();
        payroll.setEmployee(employee);
        payroll.setMonth(dto.getMonth());
        payroll.setYear(dto.getYear());
        payroll.setBasicSalary(round(basicSalary));
        payroll.setPerDaySalary(round(perDaySalary));
        payroll.setPresentDays(report.getPresentDays());
        payroll.setAbsentDays(report.getAbsentDays());
        payroll.setLateDays(report.getLateDays());
        payroll.setBonus(round(bonus));
        payroll.setDeduction(round(totalDeduction));
        payroll.setTax(round(monthlyTax));
        payroll.setNetSalary(round(netSalary));
        payroll.setNotes(dto.getNotes());
        payroll.setPaymentStatus(PaymentStatus.PENDING);
        payroll.setGeneratedDate(LocalDateTime.now());

        return mapToDto(payrollRepository.save(payroll));
    }

    // ── GENERATE BULK PAYROLL ────────────────────────────────────────

    @Override
    @Transactional
    public List<PayrollResponseDto> generateBulkPayroll(
            PayrollBulkRequestDto dto) {

        List<Employee> employees = employeeRepository.findAll();
        List<PayrollResponseDto> results = new ArrayList<>();
        List<String> skipped = new ArrayList<>();

        for (Employee employee : employees) {
            // Skip if payroll already exists
            if (payrollRepository.existsByEmployeeIdAndMonthAndYear(
                    employee.getId(), dto.getMonth(), dto.getYear())) {
                skipped.add(employee.getFirstName()
                        + " " + employee.getLastName());
                continue;
            }

            try {
                PayrollRequestDto request = new PayrollRequestDto();
                request.setEmployeeId(employee.getId());
                request.setMonth(dto.getMonth());
                request.setYear(dto.getYear());
                request.setBonus(dto.getBonusForAll());
                results.add(generatePayroll(request));
            } catch (Exception e) {
                // Skip employees with errors (e.g. no attendance data)
                skipped.add(employee.getFirstName()
                        + " " + employee.getLastName()
                        + " (" + e.getMessage() + ")");
            }
        }

        if (!skipped.isEmpty()) {
            System.out.println("Skipped employees: " + skipped);
        }

        return results;
    }

    // ── MARK AS PAID ─────────────────────────────────────────────────

    @Override
    @Transactional
    public PayrollResponseDto markAsPaid(Long payrollId) {
        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payroll not found: " + payrollId));

        if (payroll.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalArgumentException(
                    "Payroll is already marked as PAID");
        }

        if (payroll.getPaymentStatus() == PaymentStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Cannot pay a CANCELLED payroll");
        }

        payroll.setPaymentStatus(PaymentStatus.PAID);
        payroll.setPaidDate(LocalDateTime.now());

        return mapToDto(payrollRepository.save(payroll));
    }

    // ── CANCEL PAYROLL ───────────────────────────────────────────────

    @Override
    @Transactional
    public PayrollResponseDto cancelPayroll(Long payrollId) {
        Payroll payroll = payrollRepository.findById(payrollId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payroll not found: " + payrollId));

        if (payroll.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalArgumentException(
                    "Cannot cancel an already PAID payroll");
        }

        payroll.setPaymentStatus(PaymentStatus.CANCELLED);
        return mapToDto(payrollRepository.save(payroll));
    }

    // ── GET METHODS ──────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PayrollResponseDto getPayrollById(Long id) {
        return mapToDto(payrollRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Payroll not found: " + id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollResponseDto> getPayrollByEmployee(Long employeeId) {
        return payrollRepository.findByEmployeeId(employeeId)
                .stream().map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollResponseDto> getPayrollByMonthAndYear(
            int month, int year) {
        return payrollRepository.findByMonthAndYear(month, year)
                .stream().map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PayrollResponseDto> getUnpaidPayrolls() {
        return payrollRepository
                .findByPaymentStatus(PaymentStatus.PENDING.name())
                .stream().map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Double getTotalPayrollCostByMonthAndYear(int month, int year) {
        return payrollRepository.findByMonthAndYear(month, year)
                .stream()
                .filter(p -> p.getPaymentStatus() != PaymentStatus.CANCELLED)
                .mapToDouble(Payroll::getNetSalary)
                .sum();
    }

    // ── TAX CALCULATION (Pakistan Tax Slabs) ─────────────────────────

    private double calculateMonthlyTax(double annualSalary) {
        double annualTax;

        if (annualSalary <= TAX_SLAB_1_LIMIT) {
            annualTax = 0;                                      // 0%
        } else if (annualSalary <= TAX_SLAB_2_LIMIT) {
            annualTax = (annualSalary - TAX_SLAB_1_LIMIT)
                    * 0.05;                                     // 5%
        } else if (annualSalary <= TAX_SLAB_3_LIMIT) {
            annualTax = (TAX_SLAB_2_LIMIT - TAX_SLAB_1_LIMIT) * 0.05
                    + (annualSalary - TAX_SLAB_2_LIMIT) * 0.10; // 10%
        } else if (annualSalary <= TAX_SLAB_4_LIMIT) {
            annualTax = (TAX_SLAB_2_LIMIT - TAX_SLAB_1_LIMIT) * 0.05
                    + (TAX_SLAB_3_LIMIT - TAX_SLAB_2_LIMIT) * 0.10
                    + (annualSalary - TAX_SLAB_3_LIMIT) * 0.15; // 15%
        } else if (annualSalary <= TAX_SLAB_5_LIMIT) {
            annualTax = (TAX_SLAB_2_LIMIT - TAX_SLAB_1_LIMIT) * 0.05
                    + (TAX_SLAB_3_LIMIT - TAX_SLAB_2_LIMIT) * 0.10
                    + (TAX_SLAB_4_LIMIT - TAX_SLAB_3_LIMIT) * 0.15
                    + (annualSalary - TAX_SLAB_4_LIMIT) * 0.20; // 20%
        } else {
            annualTax = (TAX_SLAB_2_LIMIT - TAX_SLAB_1_LIMIT) * 0.05
                    + (TAX_SLAB_3_LIMIT - TAX_SLAB_2_LIMIT) * 0.10
                    + (TAX_SLAB_4_LIMIT - TAX_SLAB_3_LIMIT) * 0.15
                    + (TAX_SLAB_5_LIMIT - TAX_SLAB_4_LIMIT) * 0.20
                    + (annualSalary - TAX_SLAB_5_LIMIT) * 0.25; // 25%
        }

        return annualTax / 12;  // return monthly tax
    }

    // ── HELPERS ──────────────────────────────────────────────────────

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    // ── MAPPER ───────────────────────────────────────────────────────

    private PayrollResponseDto mapToDto(Payroll p) {
        PayrollResponseDto dto = new PayrollResponseDto();
        dto.setId(p.getId());
        dto.setEmployeeId(p.getEmployee().getId());
        dto.setEmployeeName(p.getEmployee().getFirstName()
                + " " + p.getEmployee().getLastName());
        dto.setDepartment(p.getEmployee().getDepartment().getName());
        dto.setDesignation(p.getEmployee().getDesignation().getTitle());
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
