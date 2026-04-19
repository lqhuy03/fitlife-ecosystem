package com.fitlife.progress_tracking.service;

import com.fitlife.progress_tracking.dto.MemberProgressSummaryResponse;
import org.springframework.transaction.annotation.Transactional;

public interface ProgressFacadeService {
    @Transactional(readOnly = true)
    MemberProgressSummaryResponse getMyProgress(Long memberId);
}