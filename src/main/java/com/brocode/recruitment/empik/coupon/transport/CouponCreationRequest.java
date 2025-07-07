package com.brocode.recruitment.empik.coupon.transport;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
public class CouponCreationRequest {
    @NotBlank(message = "Uuid is mandatory!")
    String uuid;
    @NotBlank(message = "Locale is mandatory!")
    String locale;
    @Past
    LocalDateTime creationDate;
    @Min(value = 1, message = "Maximum usage needs to be grater then 0!")
    int maxUse;
}
