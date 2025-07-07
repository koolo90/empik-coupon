package com.brocode.recruitment.empik.coupon.repository;

import com.brocode.recruitment.empik.coupon.model.Redemption;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RedemptionRepository extends CrudRepository<Redemption, Long> {
    List<Redemption> findAllByCouponId(Long couponId);

    long countAllByHolderAndCouponId(String user, Long couponId);
}
