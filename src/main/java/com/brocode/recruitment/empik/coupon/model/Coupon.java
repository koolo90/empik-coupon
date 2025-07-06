package com.brocode.recruitment.empik.coupon.model;

import com.brocode.recruitment.empik.coupon.transport.CouponCreationRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
@Entity
public class Coupon {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "empik_coupon_seq")
    @SequenceGenerator(name="empik_coupon_seq", sequenceName = "empik_coupon_id_seq", initialValue = 73570000, allocationSize = 1)
    private Long id;
    @NonNull @Column(unique = true) private String uuid;
    @NonNull private String locale;
    @NonNull private LocalDateTime creationDate;
    private int maxUse;

    public Coupon(CouponCreationRequest couponCreationRequest) {
        this.uuid = couponCreationRequest.getUuid();
        this.locale = couponCreationRequest.getLocale();
        this.creationDate = couponCreationRequest.getCreationDate();
        this.maxUse = couponCreationRequest.getMaxUse();
    }
}
