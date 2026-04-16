package com.fitlife.analytics.service;

import java.util.Map;

public interface RevenueAnalyticsService {
    Double getTotalRevenue();
    Map<String, Double> getMonthlyRevenue();
}