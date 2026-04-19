package com.fitlife.attendance.service;

import com.fitlife.attendance.dto.CheckInResponse;

public interface CheckInService {
    CheckInResponse processCheckIn(Long memberId, String actorUsername);
}
