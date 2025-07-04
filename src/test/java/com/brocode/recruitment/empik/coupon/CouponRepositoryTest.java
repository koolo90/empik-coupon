package com.brocode.recruitment.empik.coupon;

import com.brocode.recruitment.empik.coupon.model.Coupon;
import com.brocode.recruitment.empik.coupon.repository.CouponRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

@ActiveProfiles("itest")
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CouponRepositoryTest {
    @Autowired CouponRepository repository;

    @Test
    @Sql("initial_data.sql")
    void couponFound() {
        String uuid = "UUID-TEST-OK";
        Optional<Coupon> couponByUUID = repository.findCouponByUuid(uuid);
        Assertions.assertThat(couponByUUID).isPresent();
    }

    @Test
    @Sql("initial_data.sql")
    void couponNotFound() {
        String uuid = "UUID-TEST-NOK";
        Optional<Coupon> couponByUUID = repository.findCouponByUuid(uuid);
        Assertions.assertThat(couponByUUID).isEmpty();
    }
}
