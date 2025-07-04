package com.brocode.recruitment.empik.coupon;

import com.brocode.recruitment.empik.coupon.repository.CouponRepository;
import com.brocode.recruitment.empik.coupon.service.CouponController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxmind.geoip2.DatabaseReader;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("itest")
class EmpikCouponApplicationTests {
    CouponController controller;
    CouponRepository repository;
    DatabaseReader databaseReader;
    ObjectMapper mapper;

    @Test
    void contextLoads() {
        Assertions.assertThat(controller).isNotNull();
        Assertions.assertThat(repository).isNotNull();
        Assertions.assertThat(databaseReader).isNotNull();
        Assertions.assertThat(mapper).isNotNull();
    }

}
