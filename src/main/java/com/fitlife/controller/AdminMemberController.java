package com.fitlife.controller;

import com.fitlife.dto.ApiResponse;
import com.fitlife.dto.MemberResponse;
import com.fitlife.dto.PageResponse;
import com.fitlife.service.impl.MemberServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/members") // Chuẩn đường dẫn Frontend đang gọi
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')") // Bảo mật 2 lớp
public class AdminMemberController {

    private final MemberServiceImpl memberServiceImpl;

    // 1. LẤY DANH SÁCH HỘI VIÊN (ADMIN)
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<MemberResponse>>> getAllMembers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(required = false) String keyword
    ) {
        PageResponse<MemberResponse> result = memberServiceImpl.getAllMembers(page, size, sortBy, sortDir, keyword);

        return ResponseEntity.ok(ApiResponse.<PageResponse<MemberResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Lấy danh sách hội viên thành công")
                .data(result)
                .build());
    }

    // 2. API KHÓA / MỞ KHÓA TÀI KHOẢN (TOGGLE LOCK)
    @PatchMapping("/{id}/toggle-lock")
    public ResponseEntity<ApiResponse<String>> toggleMemberLock(@PathVariable Long id) {
        memberServiceImpl.toggleMemberLock(id);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(HttpStatus.OK.value())
                .message("Cập nhật trạng thái tài khoản thành công")
                .data(null)
                .build());
    }
}