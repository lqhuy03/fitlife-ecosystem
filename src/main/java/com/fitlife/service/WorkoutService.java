package com.fitlife.service;

import com.fitlife.entity.Member;
import com.fitlife.entity.WorkoutDetail;
import com.fitlife.entity.WorkoutPlan;
import com.fitlife.repository.MemberRepository;
import com.fitlife.repository.WorkoutDetailRepository;
import com.fitlife.repository.WorkoutPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkoutService {

    private final WorkoutPlanRepository workoutPlanRepository;
    private final MemberRepository memberRepository;
    private final WorkoutDetailRepository workoutDetailRepository;

    // THÊM DÒNG NÀY ĐỂ TỐI ƯU TỐC ĐỘ ĐỌC DỮ LIỆU
    @Transactional(readOnly = true)
    public WorkoutPlan getCurrentPlan(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hội viên"));

        return workoutPlanRepository.findByMemberAndStatus(member, WorkoutPlan.PlanStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Hội viên hiện không có lịch tập nào đang kích hoạt."));
    }

    @Transactional // THÊM DÒNG NÀY ĐỂ ĐẢM BẢO AN TOÀN KHI UPDATE
    public void completeWorkoutDetail(Long detailId) {
        // 1. Tìm chi tiết bài tập theo ID
        WorkoutDetail workoutDetail = workoutDetailRepository.findById(detailId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài tập với ID: " + detailId));

        // 2. Cập nhật trạng thái hoàn thành
        workoutDetail.setIsCompleted(true);

        // 3. Lưu lại vào database
        workoutDetailRepository.save(workoutDetail);
    }
}