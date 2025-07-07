package com.brocode.recruitment.empik.coupon.repository;

import com.brocode.recruitment.empik.coupon.model.Redemption;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RedemptionRepository extends CrudRepository<Redemption, Long> {
    List<Redemption> findAllByCouponUuidIgnoreCase(String couponUuid);

    long countAllByHolderAndCouponUuidIgnoreCase(String user, String couponUuid);
}
