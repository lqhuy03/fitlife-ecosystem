package com.fitlife.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fitlife.dto.AiWorkoutRequest;
import com.fitlife.dto.ApiResponse;
import com.fitlife.service.AiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/workout-plan")
    public ResponseEntity<ApiResponse<JsonNode>> generatePlan(@Valid @RequestBody AiWorkoutRequest request) {

        // Gọi Service nhờ AI tạo phác đồ
        JsonNode aiPlan = aiService.generateWorkoutPlan(request);

        // Trả kết quả JSON siêu đẹp về cho người dùng
        return ResponseEntity.ok(
                ApiResponse.<JsonNode>builder()
                        .code(200)
                        .message("Phác đồ tập luyện và dinh dưỡng cá nhân hóa đã được AI tạo thành công!")
                        .data(aiPlan)
                        .build()
        );
    }
}