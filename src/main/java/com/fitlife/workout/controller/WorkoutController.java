package com.fitlife.workout.controller;

import com.fitlife.workout.service.WorkoutService;
import com.fitlife.workout.entity.WorkoutPlan;
import com.fitlife.core.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/workout")
@RequiredArgsConstructor
@Tag(name = "Workout Management", description = "Quản lý lịch tập và tiến độ tập luyện")
public class WorkoutController {

    private final WorkoutService workoutService;

    @GetMapping("/current")
    @PreAuthorize("hasAnyAuthority('MEMBER', 'ROLE_MEMBER')")
    public ResponseEntity<ApiResponse<WorkoutPlan>> getCurrentPlan(Principal principal) {

        WorkoutPlan plan = workoutService.getCurrentPlanByUsername(principal.getName());

        return ResponseEntity.ok(ApiResponse.success(plan, "Lấy lịch tập hiện tại thành công"));
    }

    @PatchMapping("/detail/{detailId}/toggle")
    @PreAuthorize("hasAnyAuthority('MEMBER', 'ROLE_MEMBER')")
    public ResponseEntity<ApiResponse<String>> toggleWorkoutDetail(@PathVariable Long detailId) {

        workoutService.toggleWorkoutDetailStatus(detailId);

        return ResponseEntity.ok(ApiResponse.success(
                "Detail ID: " + detailId,
                "Cập nhập trạng thái bài tập thành công"));
    }
}