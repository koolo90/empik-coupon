package com.brocode.recruitment.empik.coupon.model;

import jakarta.persistence.*;
import lombok.*;

@Data @AllArgsConstructor @RequiredArgsConstructor @Builder
@Entity @Table(schema = "empik_coupon")
public class Redemption {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "empik_redemption_seq")
    @SequenceGenerator(name="empik_redemption_seq", sequenceName = "empik_redemption_id_seq", initialValue = 73570000, allocationSize = 1)
    private Long id;
    @Column private String holder;
    @ManyToOne private Coupon coupon;
}
