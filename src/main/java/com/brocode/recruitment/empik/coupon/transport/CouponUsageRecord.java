package com.brocode.recruitment.empik.coupon.transport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor
public class CouponUsageRecord {
        String uuid;
        String user;
        Integer usageCount;
}
