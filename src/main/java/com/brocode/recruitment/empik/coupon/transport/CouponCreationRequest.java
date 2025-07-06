package com.brocode.recruitment.empik.coupon.transport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class CouponCreationRequest {
    String uuid;
    String locale;
    LocalDateTime creationDate;
    int maxUse;
}
