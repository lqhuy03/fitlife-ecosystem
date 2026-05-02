package com.fitlife.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(name = "MemberProfileResponse", description = "Thông tin hồ sơ hội viên")
public class MemberProfileResponse {
    @Schema(description = "ID hội viên", example = "100")
    private Long id;
    @Schema(description = "ID tài khoản liên kết", example = "10")
    private Long userId;
    @Schema(description = "Họ và tên", example = "Nguyen Van A")
    private String fullName;
    @Schema(description = "Số điện thoại", example = "0912345678")
    private String phone;
    @Schema(description = "Địa chỉ email", example = "member01@fitlife.local")
    private String email;
    @Schema(description = "Trạng thái hồ sơ", example = "ACTIVE")
    private String status;
    @Schema(description = "Đường dẫn avatar", example = "https://cdn.fitlife.local/avatar/100.jpg")
    private String avatarUrl;
    @Schema(description = "Chiều cao (cm)", example = "175")
    private Double height;
    @Schema(description = "Cân nặng (kg)", example = "70")
    private Double weight;
    @Schema(description = "Chỉ số BMI", example = "22.86")
    private Double bmi;
    @Schema(description = "Mục tiêu fitness", example = "Giảm cân")
    private String fitnessGoal;
}