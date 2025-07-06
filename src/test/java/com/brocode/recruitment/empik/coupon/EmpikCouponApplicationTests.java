package com.brocode.recruitment.empik.coupon;

import com.brocode.recruitment.empik.coupon.repository.CouponRepository;
import com.brocode.recruitment.empik.coupon.repository.RedemptionRepository;
import com.brocode.recruitment.empik.coupon.service.CouponController;
import com.maxmind.geoip2.DatabaseReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmpikCouponApplicationTests {
    @Autowired CouponController couponController;
    @Autowired CouponRepository couponRepository;
    @Autowired RedemptionRepository redemptionRepository;
    @Autowired DatabaseReader databaseReader;

    @Test
    void contextLoads() {
        Assertions.assertNotNull(couponController);
        Assertions.assertNotNull(couponRepository);
        Assertions.assertNotNull(redemptionRepository);
        Assertions.assertNotNull(databaseReader);
    }
}