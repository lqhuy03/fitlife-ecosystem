package com.fitlife.packagegym.controller;

import com.fitlife.core.response.ApiResponse;
import com.fitlife.core.response.PageResponse;
import com.fitlife.packagegym.dto.GymPackageRequest;
import com.fitlife.packagegym.dto.GymPackageResponse;
import com.fitlife.packagegym.service.GymPackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/packages")
@RequiredArgsConstructor
public class GymPackageController {

    private final GymPackageService packageService;

    // =================================================================
    // 🌍 PUBLIC APIs (Dành cho mọi người, kể cả chưa đăng nhập)
    // =================================================================

    /**
     * API xem danh sách gói tập (Dành cho Khách/Hội viên)
     * Lưu ý của Tech Lead: Ở Service, em nhớ chỉ query các gói có status = 'ACTIVE' nhé!
     */
    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<PageResponse<GymPackageResponse>>> getActivePackages(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(required = false) String keyword
    ) {
        PageResponse<GymPackageResponse> result = packageService.getAllPackages(page, size, sortBy, sortDir, keyword);
        // CLEAN CODE: Dùng hàm success tĩnh cực kỳ gọn gàng!
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy danh sách gói tập thành công"));
    }

    /**
     * API xem chi tiết 1 gói tập
     */
    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiResponse<GymPackageResponse>> getPackageById(@PathVariable Long id) {
        GymPackageResponse result = packageService.getPackageById(id);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy thông tin chi tiết gói tập thành công"));
    }

    // =================================================================
    // 🛡️ ADMIN APIs (Chỉ dành cho Quản trị viên)
    // =================================================================

    /**
     * API cho Admin lấy TẤT CẢ gói tập (Bao gồm cả gói đã bị ẩn/Soft Delete)
     * Đường dẫn: /api/v1/packages/admin
     */
    @GetMapping("/admin")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<GymPackageResponse>>> getAllPackagesForAdmin(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(required = false) String keyword
    ) {
        PageResponse<GymPackageResponse> result = packageService.getAllPackages(page, size, sortBy, sortDir, keyword);
        return ResponseEntity.ok(ApiResponse.success(result, "Lấy danh sách quản lý gói tập (Admin) thành công"));
    }

    /**
     * API Tạo gói tập mới
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<GymPackageResponse>> createPackage(@Valid @RequestBody GymPackageRequest request) {
        GymPackageResponse result = packageService.createPackage(request);

        // Giữ nguyên Builder cho HTTP 201 CREATED để Code trong JSON khớp với Status HTTP
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<GymPackageResponse>builder()
                        .code(201)
                        .message("Tạo gói tập mới thành công")
                        .data(result)
                        .build()
        );
    }

    /**
     * API Cập nhật gói tập
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<GymPackageResponse>> updatePackage(
            @PathVariable Long id,
            @Valid @RequestBody GymPackageRequest request) {

        GymPackageResponse result = packageService.updatePackage(id, request);
        return ResponseEntity.ok(ApiResponse.success(result, "Cập nhật thông tin gói tập thành công"));
    }

    /**
     * API Ẩn/Hiện gói tập (Soft Delete)
     */
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<String>> togglePackageStatus(@PathVariable Long id) {
        packageService.togglePackageStatus(id);

        // Truyền null cho phần data vì thao tác này chỉ cần message thành công
        return ResponseEntity.ok(ApiResponse.success(null, "Cập nhật trạng thái gói tập thành công"));
    }
}