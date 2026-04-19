package com.fitlife.subscription.service;

import com.fitlife.subscription.dto.SubscriptionCreationRequest;
import com.fitlife.subscription.dto.SubscriptionResponse;

public interface SubscriptionService {
    SubscriptionResponse createSubscription(String username, SubscriptionCreationRequest request);
}
