package com.example.hrm.system.emums;

public enum AttendanceStatus {
    PRESENT,    // checked in on time
    LATE,       // checked in after 9:30 AM
    ABSENT,     // never checked in (auto-marked at 11:59 PM)
    HALF_DAY,   // checked out before 1:00 PM
    ON_LEAVE    // on approved leave
}