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
import java.time.Month;
import java.util.Optional;

@ActiveProfiles("itest")
@DataJpaTest
class CouponRepositoryTest {
    @Autowired CouponRepository repository;

    private final String correctUuid = "UUID-TEST-OK";
    private final String wrongUuid = "UUID-TEST-NOK";
    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDateTime oldDate = LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0);
    private final String correctLocale = "US";
    private final String wrongLocalee = "PL";

    @Test
    @Sql("initial_data.sql")
    void couponFound() {
        Optional<Coupon> couponByUUID = repository.findCouponByUuidAndLocaleAndCreationDateBefore(correctUuid, correctLocale,
                now);

        Assertions.assertThat(couponByUUID).isPresent();
    }

    @Test
    @Sql("initial_data.sql")
    void notFound_wrongUuid() {
        Optional<Coupon> couponByUUID = repository.findCouponByUuidAndLocaleAndCreationDateBefore(wrongUuid, correctLocale,
                now);

        Assertions.assertThat(couponByUUID).isEmpty();
    }

    @Test
    @Sql("initial_data.sql")
    void notFound_wrongLocale() {
        Optional<Coupon> couponByUUID = repository.findCouponByUuidAndLocaleAndCreationDateBefore(correctUuid, wrongLocalee,
                now);

        Assertions.assertThat(couponByUUID).isEmpty();
    }

    @Test
    @Sql("initial_data.sql")
    void notFound_wrongDate() {
        Optional<Coupon> couponByUUID = repository.findCouponByUuidAndLocaleAndCreationDateBefore(correctUuid, correctLocale,
                oldDate);

        Assertions.assertThat(couponByUUID).isEmpty();
    }
}
