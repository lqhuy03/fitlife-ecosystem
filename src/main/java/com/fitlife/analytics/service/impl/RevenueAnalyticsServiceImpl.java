package com.fitlife.analytics.service.impl;

import com.fitlife.analytics.service.RevenueAnalyticsService;
import com.fitlife.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RevenueAnalyticsServiceImpl implements RevenueAnalyticsService {

    private final PaymentRepository paymentRepository;

    @Override
    public Double getTotalRevenue() {
        Double totalRev = paymentRepository.getTotalRevenue();
        return totalRev != null ? totalRev : 0.0;
    }

    @Override
    public Map<String, Double> getMonthlyRevenue() {
        List<Object[]> monthlyData = paymentRepository.getMonthlyRevenueMapping();
        Map<String, Double> revenueMap = new HashMap<>();

        if (monthlyData != null) {
            for (Object[] obj : monthlyData) {
                revenueMap.put("Tháng " + obj[0], (Double) obj[1]);
            }
        }
        return revenueMap;
    }
}