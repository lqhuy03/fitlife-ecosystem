package com.fitlife.member.controller;

import com.fitlife.core.response.ApiResponse;
import com.fitlife.core.response.PageResponse;
import com.fitlife.member.dto.MemberCreationRequest;
import com.fitlife.member.dto.MemberProfileResponse;
import com.fitlife.member.dto.MemberUpdateRequest;
import com.fitlife.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
@Tag(name = "Member Management", description = "Quản lý hồ sơ hội viên và thao tác dành cho admin/staff")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    @Operation(summary = "Tạo hồ sơ hội viên", description = "Tạo mới hồ sơ hội viên cho tài khoản đã có sẵn trong hệ thống.")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> createMember(@Valid @RequestBody MemberCreationRequest request) {
        MemberProfileResponse result = memberService.createMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result, "Member created successfully"));
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Cập nhật avatar hội viên", description = "Upload ảnh đại diện mới cho hội viên hiện tại.")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @Parameter(description = "Tệp ảnh cần upload")
            @RequestParam("file") MultipartFile file,
            Principal principal) throws IOException {
        String avatarUrl = memberService.updateAvatar(principal.getName(), file);
        return ResponseEntity.ok(ApiResponse.success(avatarUrl, "Cập nhật ảnh đại diện thành công"));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Lấy danh sách hội viên (Admin/Staff)", description = "Hỗ trợ phân trang, sắp xếp và tìm kiếm danh sách hội viên.")
    public ResponseEntity<ApiResponse<PageResponse<MemberProfileResponse>>> getAllMembers(
            @Parameter(description = "Trang hiện tại, bắt đầu từ 1", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "Kích thước trang", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường dùng để sắp xếp", example = "id")
            @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Chiều sắp xếp: ASC hoặc DESC", example = "DESC")
            @RequestParam(defaultValue = "DESC") String sortDir,
            @Parameter(description = "Từ khóa tìm kiếm theo tên/số điện thoại/email", example = "Nguyen")
            @RequestParam(required = false) String keyword) {

        PageResponse<MemberProfileResponse> result = memberService.getAllMembers(page, size, sortBy, sortDir, keyword);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy danh sách hội viên thành công"));
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Tạo hội viên và tài khoản (Admin)", description = "Admin tạo hồ sơ hội viên đồng thời khởi tạo tài khoản đăng nhập.")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> createMemberByAdmin(@Valid @RequestBody MemberCreationRequest request) {
        MemberProfileResponse result = memberService.createMemberByAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(result, "Thêm hội viên và tạo tài khoản thành công"));
    }

    @PatchMapping("/admin/{id}/toggle-lock")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Khóa/mở khóa hội viên", description = "Chuyển trạng thái khóa tài khoản của hội viên theo ID.")
    public ResponseEntity<ApiResponse<String>> toggleMemberLock(@PathVariable Long id) {
        memberService.toggleMemberLock(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Cập nhật trạng thái tài khoản thành công"));
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Lấy chi tiết hội viên", description = "Tra cứu thông tin một hội viên cụ thể theo ID.")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> getMemberById(@PathVariable Long id) {
        MemberProfileResponse result = memberService.getMemberById(id);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy thông tin hội viên thành công"));
    }

    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cập nhật hội viên", description = "Cập nhật thông tin hội viên theo ID cho quản trị viên.")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> updateMemberByAdmin(
            @PathVariable Long id,
            @Valid @RequestBody MemberCreationRequest request) {
        MemberProfileResponse result = memberService.updateMemberByAdmin(id, request);
        return ResponseEntity.ok(ApiResponse.success(result, "Cập nhật thông tin hội viên thành công"));
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Xóa hội viên", description = "Xóa hội viên khỏi hệ thống theo ID.")
    public ResponseEntity<ApiResponse<String>> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Đã xóa hội viên khỏi hệ thống"));
    }

    @GetMapping("/me")
    @Operation(summary = "Lấy hồ sơ cá nhân", description = "Lấy thông tin profile dựa trên Token đang đăng nhập (Bảo mật IDOR)")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> getMyProfile(Principal principal) {
        // principal.getName() sẽ tự động móc cái username ra từ JWT Token
        MemberProfileResponse result = memberService.getMyProfile(principal.getName());
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy hồ sơ cá nhân thành công"));
    }

    @PutMapping("/me")
    @Operation(summary = "Cập nhật hồ sơ & Chỉ số BMI", description = "Cập nhật thông tin cá nhân và hệ thống tự tính lại BMI")
    public ResponseEntity<ApiResponse<MemberProfileResponse>> updateMyProfile(
            @Valid @RequestBody MemberUpdateRequest request,
            Principal principal) {
        MemberProfileResponse result = memberService.updateMyProfile(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(result, "Cập nhật hồ sơ thành công"));
    }
}