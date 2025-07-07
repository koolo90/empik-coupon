package com.brocode.recruitment.empik.coupon.transport;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class RedemptionRequest {
        @NotBlank(message = "Uuid is mandatory!")
        String couponUuid;
        @NotBlank(message = "USer is mandatory!")
        String user;
        @Min(value = 1, message = "Usage needs to be grater then 0!")
        int usageCount;
        boolean localize;
}
