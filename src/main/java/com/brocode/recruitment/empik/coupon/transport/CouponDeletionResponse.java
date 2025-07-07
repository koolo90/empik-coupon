package com.brocode.recruitment.empik.coupon.transport;

import com.brocode.recruitment.empik.coupon.model.Coupon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouponDeletionResponse {
    Coupon deletedCoupon;
    String message;
}
