package com.brocode.recruitment.empik.coupon.transport;


import com.brocode.recruitment.empik.coupon.model.Redemption;
import lombok.*;
import org.springframework.http.HttpStatus;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class RedemptionResponse {
        Redemption persistedRedemption;
        @NonNull RedemptionRequest redemptionRequest;
        HttpStatus httpStatus;
        String errorMessage;
        boolean success;
        StackTraceElement[] stackTrace;
}