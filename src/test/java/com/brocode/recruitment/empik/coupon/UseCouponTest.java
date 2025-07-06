package com.brocode.recruitment.empik.coupon;

import com.brocode.recruitment.empik.coupon.model.Coupon;
import com.brocode.recruitment.empik.coupon.repository.CouponRepository;
import com.brocode.recruitment.empik.coupon.repository.RedemptionRepository;
import com.brocode.recruitment.empik.coupon.service.CouponController;
import com.brocode.recruitment.empik.coupon.transport.CouponCreationRequest;
import com.brocode.recruitment.empik.coupon.transport.RedemptionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CouponController.class)
class UseCouponTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean CouponRepository couponRepository;
    @MockitoBean RedemptionRepository redemptionRepository;

    private String couponServiceUrl;
    private String usIpAddress;
    private String plIpAddress;

    UseCouponTest() {
        objectMapper = new ObjectMapper();
        plIpAddress = "103.112.60.1";
        usIpAddress = "8.8.8.8";
        couponServiceUrl = "/coupon";
    }

    @Test
    void createCoupon() throws Exception {
        CouponCreationRequest build = CouponCreationRequest.builder().build();
        mockMvc.perform(post(couponServiceUrl + "/new")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(build))
                .characterEncoding("utf-8")
        ).andExpect(status().isOk());
    }

    @Test
    void deleteAll() throws Exception {
        mockMvc.perform(delete(couponServiceUrl + "/drop/all")).andExpect(status().isOk());
    }

    @Test
    void use() throws Exception {
        Coupon coupon = Coupon.builder().uuid("TEST").locale("US").maxUse(1).creationDate(LocalDateTime.MIN).build();
        when(couponRepository.findCouponByUuidAndLocaleAndCreationDateBefore(any(), any(), any())).thenReturn(Optional.of(coupon));
        RedemptionRequest redemptionRequest = RedemptionRequest.builder().usageCount(1).build();
        mockMvc.perform(post(couponServiceUrl + "/redeem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(redemptionRequest))
                .with(remoteHost(usIpAddress))
                .characterEncoding("utf-8")
        ).andExpect(status().isOk());
    }

    private static RequestPostProcessor remoteHost(final String remoteHost) {
        return request -> {
            request.setRemoteAddr(remoteHost);
            return request;
        };
    }
}