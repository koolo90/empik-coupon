package com.brocode.recruitment.empik.coupon;

import com.brocode.recruitment.empik.coupon.model.Coupon;
import com.brocode.recruitment.empik.coupon.repository.CouponRepository;
import com.brocode.recruitment.empik.coupon.repository.RedemptionRepository;
import com.brocode.recruitment.empik.coupon.service.CouponController;
import com.brocode.recruitment.empik.coupon.transport.CouponCreationRequest;
import com.brocode.recruitment.empik.coupon.transport.RedemptionRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//FINISHED
@WebMvcTest(controllers = CouponController.class)
class UseCouponTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean CouponRepository couponRepository;
    @MockitoBean RedemptionRepository redemptionRepository;

    private final String couponServiceUrl;
    private final String usIpAddress;

    UseCouponTest() {
        objectMapper = new ObjectMapper();
        usIpAddress = "8.8.8.8";
        couponServiceUrl = "/coupon";
    }

    @Test
    void createCoupon() throws Exception {
        CouponCreationRequest build = CouponCreationRequest.builder()
                .creationDate(LocalDateTime.of(2020, 10, 10, 10, 10, 10))
                .maxUse(10)
                .locale("US")
                .uuid("SPRING")
                .build();
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
        RedemptionRequest redemptionRequest = RedemptionRequest.builder()
                .user("Max")
                .couponUuid("SPRING")
                .usageCount(1).build();
        MvcResult mvcResult = mockMvc.perform(post(couponServiceUrl + "/redeem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(redemptionRequest))
                .with(remoteHost(usIpAddress))
                .characterEncoding("utf-8")
        ).andExpect(status().isOk()).andReturn();
        Assertions.assertNotNull(mvcResult.getResponse().getContentAsString());
    }

    private static RequestPostProcessor remoteHost(final String remoteHost) {
        return request -> {
            request.setRemoteAddr(remoteHost);
            return request;
        };
    }
}