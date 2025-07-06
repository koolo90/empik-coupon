package com.brocode.recruitment.empik.coupon.transport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class RedemptionRequest {
        String couponUuid;
        String user;
        Integer usageCount;
}
