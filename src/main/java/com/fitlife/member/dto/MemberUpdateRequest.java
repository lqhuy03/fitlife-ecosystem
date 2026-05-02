package com.fitlife.member.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberUpdateRequest {
    private String fullName;
    private String phone;

    @Min(value = 30, message = "Cân nặng phải lớn hơn 30kg")
    private Double weight; // Cân nặng (kg)

    @Min(value = 100, message = "Chiều cao phải lớn hơn 100cm")
    private Double height; // Chiều cao (cm)

    private String fitnessGoal; // Mục tiêu: Giảm cân, Tăng cơ...
}