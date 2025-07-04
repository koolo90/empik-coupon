package com.brocode.recruitment.empik.coupon.repository;

import com.brocode.recruitment.empik.coupon.model.Coupon;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CouponRepository extends CrudRepository<Coupon, Long> {
    Optional<Coupon> findCouponByUuid(String uuid);

    Optional<Coupon> findCouponByUuidAndLocale(String uuid, String isoCountryFromIpAddress);
}
