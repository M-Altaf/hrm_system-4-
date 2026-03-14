package com.example.hrm.system.dtos.requestdto;




import com.example.hrm.system.emums.LeaveType;
import lombok.Data;
import java.time.LocalDate;

@Data
public class LeaveRequestDto {
    private Long employeeId;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
}
