package com.brocode.recruitment.empik.coupon.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data @AllArgsConstructor @NoArgsConstructor @RequiredArgsConstructor @Builder
@Entity @Table(schema = "empik_coupon")
public class Coupon {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "empik_coupon_seq")
    @SequenceGenerator(name="empik_coupon_seq", sequenceName = "empik_coupon_id_seq", initialValue = 73570000, allocationSize = 1)
    private Long id;
    @NonNull @Column(unique = true) private String uuid;
    @NonNull private String locale;
    @NonNull private LocalDateTime creationDate;
    @NonNull private Integer maxUse;
    private Integer currUse;
}
