/*
package com.brocode.recruitment.empik.coupon;

import com.brocode.recruitment.empik.coupon.model.Coupon;
import com.brocode.recruitment.empik.coupon.repository.CouponRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.Optional;

@ActiveProfiles("itest")
@DataJpaTest
class CouponRepositoryTest {
    @Autowired CouponRepository repository;

    @Test
    @Sql("initial_data.sql")
    void couponFound() {
        repository.findAll().forEach(System.out::println);
        String uuid = "UUID-TEST-OK";
        Optional<Coupon> couponByUUID = repository.findCouponByUuidAndLocaleAndCreationDateBefore(uuid, "US",
                LocalDateTime.now());
        Assertions.assertThat(couponByUUID).isPresent();
    }
}
*/
