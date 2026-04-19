//package com.fitlife.analytics;
//
//import com.fitlife.analytics.dto.AdminDashboardResponse;
//import com.fitlife.core.response.ApiResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/admin/analytics")
//@RequiredArgsConstructor
//public class AdminAnalyticsController {
//
//    private final RevenueAnalyticsService revenueAnalyticsService;
//    private final TrafficAnalyticsService trafficAnalyticsService;
//
//    @GetMapping("/dashboard")
//    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ADMIN')")
//    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getDashboardData() {
//
//        AdminDashboardResponse dashboard = AdminDashboardResponse.builder()
//                .totalMembers(trafficAnalyticsService.getTotalMembers())
//                .activeMembers(trafficAnalyticsService.getActiveMembers())
//                .totalCheckinsToday(trafficAnalyticsService.getTotalCheckinsToday())
//                .totalRevenue(revenueAnalyticsService.getTotalRevenue())
//                .revenueByMonth(revenueAnalyticsService.getMonthlyRevenue())
//                .build();
//
//        return ResponseEntity.ok(ApiResponse.<AdminDashboardResponse>builder()
//                .code(200)
//                .message("Lấy dữ liệu thống kê Admin thành công")
//                .data(dashboard)
//                .build());
//    }
//}