package com.brocode.recruitment.empik.coupon.transport;


import com.brocode.recruitment.empik.coupon.model.Redemption;
import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class RedemptionResponse {
        Redemption persistedRedemption;
        @NonNull RedemptionRequest redemptionRequest;
        String errorMessage;
        StackTraceElement[] stackTrace;

        public RedemptionResponse(Redemption persistedRedemption) {
                this.persistedRedemption = persistedRedemption;
        }
}