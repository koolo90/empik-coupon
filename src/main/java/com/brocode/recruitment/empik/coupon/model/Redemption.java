package com.brocode.recruitment.empik.coupon.model;

import com.brocode.recruitment.empik.coupon.transport.RedemptionRequest;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data @AllArgsConstructor @NoArgsConstructor @Builder
@Entity
public class Redemption {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "empik_redemption_seq")
    @SequenceGenerator(name="empik_redemption_seq", sequenceName = "empik_redemption_id_seq", initialValue = 73570000, allocationSize = 1)
    private Long id;

    @NotNull private String holder; //user
    @NotNull private int amount;
    @NotNull private String locale;
    @NotNull String couponUuid;

    public Redemption(@Valid RedemptionRequest redemptionRequest, String couponUuid, String isoCode) {
        this.holder = redemptionRequest.getUser();
        this.amount = redemptionRequest.getUsageCount();
        this.locale = isoCode;
        this.couponUuid = couponUuid;
    }
}