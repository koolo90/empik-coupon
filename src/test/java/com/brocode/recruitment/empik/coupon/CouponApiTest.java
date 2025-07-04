package com.brocode.recruitment.empik.coupon;


import com.brocode.recruitment.empik.coupon.model.Coupon;
import com.brocode.recruitment.empik.coupon.transport.CouponUsageRecord;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Locale;

@SpringBootTest(classes = {RestClientConfig.class})
@ActiveProfiles("itest")
class CouponApiTest {
    @Autowired TestRestTemplate couponApi;

    String url = "http://%s:%d/%s/%s/%s";
    String host = "localhost";
    int port = 56155;
    String appUrl = "/empik-coupon";
    String endpointUrl = "/coupon";

    @BeforeEach
    void setup() {
        // pre-test sanitization
        this.couponApi.delete(url.formatted(host, port, appUrl, endpointUrl, "/drop/all"));

        couponApi.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        String apiUrl = url.formatted(host, port, appUrl, endpointUrl, "new");
        Coupon springCoupon = new Coupon( "WIOSNA", Locale.US.getISO3Country(), LocalDateTime.now(), 10);
        ResponseEntity<Coupon> couponResponseEntity = this.couponApi.postForEntity(apiUrl, springCoupon, Coupon.class);
        Assertions.assertThat(couponResponseEntity.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @AfterEach
    void teardown() {
        this.couponApi.delete(url.formatted(host, port, appUrl, endpointUrl, "drop/all"));
    }

    @Test
    void getNewCouponUsage() {
        String apiUrl = url.formatted(host, port, appUrl, endpointUrl, "use");
        CouponUsageRecord couponUsageRecord = new CouponUsageRecord("WIOSNA", "MAX", 10);
        CouponUsageRecord usedCouponUsageRecord = this.couponApi.patchForObject(apiUrl, couponUsageRecord, CouponUsageRecord.class);

        Assertions.assertThat(usedCouponUsageRecord).isNotNull();
        Assertions.assertThat(usedCouponUsageRecord.getUsageCount()).isEqualTo(10);
        Assertions.assertThat(usedCouponUsageRecord.getUuid()).isEqualTo("WIOSNA");
    }
}
