package com.brocode.recruitment.empik.coupon.transport;

import com.brocode.recruitment.empik.coupon.model.Coupon;
import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor @EqualsAndHashCode(callSuper=false)
public class CouponUsageRecordResponse extends CouponUsageRecord {
        String message;
        StackTraceElement[] stack;

        @Builder
        public CouponUsageRecordResponse(String uuid, String user, Integer usageCount, String message, StackTraceElement[] stack) {
                super(uuid, user, usageCount);
                this.message = message;
                this.stack = stack;
        }

        public CouponUsageRecordResponse(Coupon save) {
                super(save.getUuid(), null, save.getCurrUse());
        }
}
