package com.example.hrm.system.services;




import com.example.hrm.system.dtos.requestdto.PayrollRequestDto;
import com.example.hrm.system.dtos.responsedto.PayrollResponseDto;
import com.example.hrm.system.dtos.updatedto.PayrollBulkRequestDto;

import java.util.List;

public interface PayrollService {
    PayrollResponseDto generatePayroll(PayrollRequestDto dto);
    List<PayrollResponseDto> generateBulkPayroll(PayrollBulkRequestDto dto);
    PayrollResponseDto markAsPaid(Long payrollId);
    PayrollResponseDto cancelPayroll(Long payrollId);
    PayrollResponseDto getPayrollById(Long id);
    List<PayrollResponseDto> getPayrollByEmployee(Long employeeId);
    List<PayrollResponseDto> getPayrollByMonthAndYear(int month, int year);
    List<PayrollResponseDto> getUnpaidPayrolls();
    Double getTotalPayrollCostByMonthAndYear(int month, int year);
}
