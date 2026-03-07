package com.fitlife.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fitlife.dto.AiWorkoutRequest;
import com.fitlife.dto.ApiResponse;
import com.fitlife.entity.AiWorkoutPlan;
import com.fitlife.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller quản lý các tính năng liên quan đến Trí tuệ nhân tạo (Gemini AI)
 * Tích hợp tạo phác đồ, lưu trữ lịch sử và kích hoạt lịch tập nghiệp vụ.
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    /**
     * Endpoint: POST /api/v1/ai/workout-plan
     * Chức năng: Gọi AI phân tích chỉ số sức khỏe và mục tiêu để tạo phác đồ.
     * Kết quả được lưu tự động vào bảng ai_workout_plans dưới dạng JSON.
     */
    @PostMapping("/workout-plan")
    public ResponseEntity<ApiResponse<JsonNode>> generatePlan(@Valid @RequestBody AiWorkoutRequest request) {
        JsonNode aiPlan = aiService.generateWorkoutPlan(request);
        return ResponseEntity.ok(
                ApiResponse.<JsonNode>builder()
                        .code(200)
                        .message("Phác đồ cá nhân hóa đã được AI tạo và lưu vào lịch sử thành công!")
                        .data(aiPlan)
                        .build()
        );
    }

    /**
     * Endpoint: GET /api/v1/ai/history/{memberId}
     * Chức năng: Lấy danh sách các phác đồ cũ mà AI đã từng tư vấn cho hội viên.
     */
    @GetMapping("/history/{memberId}")
    public ResponseEntity<ApiResponse<List<AiWorkoutPlan>>> getHistory(@PathVariable Long memberId) {
        List<AiWorkoutPlan> history = aiService.getMemberHistory(memberId);
        return ResponseEntity.ok(
                ApiResponse.<List<AiWorkoutPlan>>builder()
                        .code(200)
                        .message("Lấy danh sách lịch sử tư vấn AI thành công.")
                        .data(history)
                        .build()
        );
    }

    /**
     * Endpoint: POST /api/v1/ai/activate/{planId}
     * Chức năng: Bóc tách dữ liệu JSON từ phác đồ AI và đẩy vào bảng WorkoutPlan/WorkoutSession/WorkoutDetail chính thức.
     */
    @PostMapping("/activate/{planId}")
    public ResponseEntity<ApiResponse<String>> activatePlan(@PathVariable Long planId) {
        aiService.activatePlan(planId);
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .code(200)
                        .message("Kích hoạt lịch tập thành công! Lịch tập AI hiện đã trở thành lịch tập chính thức của bạn.")
                        .data("Activated Plan ID: " + planId)
                        .build()
        );
    }
}