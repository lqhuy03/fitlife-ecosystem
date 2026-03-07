package com.fitlife.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AiWorkoutRequest {
    @NotNull(message = "Thiếu ID Hội viên")
    private Long memberId;

    @NotBlank(message = "Mục tiêu không được để trống (VD: Tăng cơ giảm mỡ)")
    private String goal;

    @NotBlank(message = "Trình độ không được để trống (VD: Beginner, Intermediate)")
    private String fitnessLevel;

    @Min(1) @Max(7)
    private int daysPerWeek;

    // Các trường Nâng cao (Có thể null)
    private String injuries;       // Chấn thương (VD: Đau lưng dưới)
    private String equipment;      // Thiết bị (VD: Chỉ có tạ đơn)
    private String dietPreference; // Ăn kiêng (VD: Ăn chay, Keto)
}