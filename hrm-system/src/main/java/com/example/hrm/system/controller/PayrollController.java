package com.example.hrm.system.controller;

import com.example.hrm.system.dtos.requestdto.PayrollRequestDto;
import com.example.hrm.system.dtos.responsedto.PayrollResponseDto;
import com.example.hrm.system.dtos.updatedto.PayrollBulkRequestDto;
import com.example.hrm.system.services.PayrollService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payrolls")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    // ── GENERATE SINGLE PAYROLL ───────────────────────────────────────
    // POST /api/payrolls/generate
    // Body: { "employeeId":1, "month":4, "year":2026, "bonus":5000, "extraDeduction":0, "notes":"April payroll" }
    @PostMapping("/generate")
    public ResponseEntity<PayrollResponseDto> generatePayroll(
            @Valid @RequestBody PayrollRequestDto dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(payrollService.generatePayroll(dto));
    }

    // ── GENERATE BULK PAYROLL ─────────────────────────────────────────
    // POST /api/payrolls/generate-bulk
    // Body: { "month":4, "year":2026, "bonusForAll":2000 }
    // Generates payroll for ALL employees for the given month
    @PostMapping("/generate-bulk")
    public ResponseEntity<List<PayrollResponseDto>> generateBulkPayroll(
            @Valid @RequestBody PayrollBulkRequestDto dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(payrollService.generateBulkPayroll(dto));
    }

    // ── MARK PAYROLL AS PAID ──────────────────────────────────────────
    // PUT /api/payrolls/{id}/paid
    @PutMapping("/{id}/paid")
    public ResponseEntity<PayrollResponseDto> markAsPaid(
            @PathVariable Long id) {
        return ResponseEntity.ok(payrollService.markAsPaid(id));
    }

    // ── CANCEL PAYROLL ────────────────────────────────────────────────
    // PUT /api/payrolls/{id}/cancel
    @PutMapping("/{id}/cancel")
    public ResponseEntity<PayrollResponseDto> cancelPayroll(
            @PathVariable Long id) {
        return ResponseEntity.ok(payrollService.cancelPayroll(id));
    }

    // ── GET PAYROLL BY ID ─────────────────────────────────────────────
    // GET /api/payrolls/{id}
    @GetMapping("/{id}")
    public ResponseEntity<PayrollResponseDto> getPayrollById(
            @PathVariable Long id) {
        return ResponseEntity.ok(payrollService.getPayrollById(id));
    }

    // ── GET ALL PAYROLLS BY EMPLOYEE ──────────────────────────────────
    // GET /api/payrolls/employee/{employeeId}
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<PayrollResponseDto>> getPayrollByEmployee(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
                payrollService.getPayrollByEmployee(employeeId));
    }

    // ── GET PAYROLLS BY MONTH AND YEAR ────────────────────────────────
    // GET /api/payrolls/month?month=4&year=2026
    @GetMapping("/month")
    public ResponseEntity<List<PayrollResponseDto>> getPayrollByMonthAndYear(
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(
                payrollService.getPayrollByMonthAndYear(month, year));
    }

    // ── GET ALL UNPAID PAYROLLS ───────────────────────────────────────
    // GET /api/payrolls/unpaid
    @GetMapping("/unpaid")
    public ResponseEntity<List<PayrollResponseDto>> getUnpaidPayrolls() {
        return ResponseEntity.ok(payrollService.getUnpaidPayrolls());
    }

    // ── GET TOTAL PAYROLL COST BY MONTH AND YEAR ──────────────────────
    // GET /api/payrolls/cost?month=4&year=2026
    // Returns total net salary of all non-cancelled payrolls for the month
    @GetMapping("/cost")
    public ResponseEntity<Double> getTotalPayrollCost(
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(
                payrollService.getTotalPayrollCostByMonthAndYear(month, year));
    }
}