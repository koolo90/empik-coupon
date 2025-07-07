package com.brocode.recruitment.empik.coupon;

import com.brocode.recruitment.empik.coupon.model.Coupon;
import com.brocode.recruitment.empik.coupon.transport.CouponCreationRequest;
import com.brocode.recruitment.empik.coupon.transport.RedemptionRequest;
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
import java.time.Month;

@SpringBootTest(classes = {RestClientConfig.class})
@ActiveProfiles("itest")
class CouponApiTest {
    @Autowired TestRestTemplate couponApi;

    String url = "http://%s:%d/%s/%s/%s";
    String host = "localhost";
    int port = 8080;
    String appUrl = "/empik-coupon";
    String endpointUrl = "/coupon";

    @BeforeEach
    void setup() {
        // pre-test sanitization
        this.couponApi.delete(url.formatted(host, port, appUrl, endpointUrl, "/drop/all"));

        couponApi.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        String apiUrl = url.formatted(host, port, appUrl, endpointUrl, "new");
        CouponCreationRequest springCoupon = CouponCreationRequest.builder()
                .uuid("SPRING")
                .locale("US")
                .creationDate(LocalDateTime.of(2025, Month.JANUARY, 01, 00, 00, 00))
                .maxUse(5)
                .build();
        ResponseEntity<Coupon> couponResponseEntity = this.couponApi.postForEntity(apiUrl, springCoupon, Coupon.class);
        Assertions.assertThat(couponResponseEntity.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void use() {
        String apiUrl = url.formatted(host, port, appUrl, endpointUrl, "redeem");
        RedemptionRequest redemptionRequest = RedemptionRequest.builder().localize(false).couponUuid("SPRING").user("Max").usageCount(1).build();
        ResponseEntity<Coupon> couponResponseEntity = this.couponApi.postForEntity(apiUrl, redemptionRequest, Coupon.class);
        Assertions.assertThat(couponResponseEntity.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @AfterEach
    void teardown() {
        this.couponApi.delete(url.formatted(host, port, appUrl, endpointUrl, "drop/all"));
    }
}