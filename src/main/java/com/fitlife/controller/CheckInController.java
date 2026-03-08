package com.fitlife.controller;

import com.fitlife.dto.ApiResponse;
import com.fitlife.dto.CheckInResponse;
import com.fitlife.entity.User;
import com.fitlife.repository.UserRepository;
import com.fitlife.service.CheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/checkin") // Đã thêm /api/v1 cho chuẩn cấu trúc
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;
    private final UserRepository userRepository;

    /**
     * 1. Luồng Staff/Admin: Lễ tân quét thẻ/mã của khách hàng
     */
    @PostMapping("/{memberId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<CheckInResponse>> staffProcessCheckIn(
            @PathVariable Long memberId,
            Authentication authentication) {

        // authentication.getName() sẽ lấy ra username của Staff đang thực hiện
        CheckInResponse result = checkInService.processCheckIn(memberId, authentication.getName());

        return ResponseEntity.ok(ApiResponse.<CheckInResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Check-in processed by staff")
                .data(result)
                .build());
    }

    /**
     * 2. Luồng Self-Service: Member tự mở App quét mã tại cửa
     */
    @PostMapping("/me")
    @PreAuthorize("hasRole('MEMBER')")
    public ResponseEntity<ApiResponse<CheckInResponse>> memberSelfCheckIn(Authentication authentication) {

        // Tìm user đang đăng nhập dựa vào Token
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Tài khoản không hợp lệ"));

        if (user.getMember() == null) {
            throw new RuntimeException("Tài khoản này chưa có hồ sơ hội viên!");
        }

        // Truyền ID của chính Member đó xuống Service
        CheckInResponse result = checkInService.processCheckIn(user.getMember().getId(), authentication.getName());

        return ResponseEntity.ok(ApiResponse.<CheckInResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Self check-in processed")
                .data(result)
                .build());
    }
}