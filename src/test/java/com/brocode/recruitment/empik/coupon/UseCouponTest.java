package com.brocode.recruitment.empik.coupon;

import com.brocode.recruitment.empik.coupon.model.Coupon;
import com.brocode.recruitment.empik.coupon.repository.CouponRepository;
import com.brocode.recruitment.empik.coupon.service.CouponController;
import com.brocode.recruitment.empik.coupon.transport.CouponUsageRecord;
import com.brocode.recruitment.empik.coupon.transport.CouponUsageRecordResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CouponController.class)
@ActiveProfiles("utest")
class UseCouponTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean CouponRepository repository;
    @Captor ArgumentCaptor<Coupon> captor;

    private String couponServiceUrl;
    private String usIpAddress;
    private String plIpAddress;
    private Coupon unusedCoupon;
    private Coupon partiallyConsumedCoupon;
    private Coupon consumedCoupon;
    @Autowired ObjectMapper objectMapper;

    UseCouponTest() {
        consumedCoupon = Coupon.builder()
                .id(7357L)
                .uuid("UUID-TEST-CONSUMED")
                .creationDate(LocalDateTime.now())
                .locale(Locale.US.getCountry())
                .maxUse(100).currUse(100).build();
        partiallyConsumedCoupon = Coupon.builder()
                .id(7357L)
                .uuid("UUID-TEST-PARTIALY-CONSUMED")
                .creationDate(LocalDateTime.now())
                .locale(Locale.US.getCountry())
                .maxUse(100).currUse(90).build();
        unusedCoupon = Coupon.builder()
                .id(7357L)
                .uuid("UUID-TEST-OK")
                .creationDate(LocalDateTime.now())
                .locale(Locale.US.getISO3Country())
                .maxUse(100).currUse(0).build();
        objectMapper = new ObjectMapper();
        plIpAddress = "103.112.60.1";
        usIpAddress = "8.8.8.8";
        couponServiceUrl = "/coupon";
    }

    @Test
    void createNewCoupon() throws Exception {
        String jsonContent = objectMapper.writeValueAsString(unusedCoupon);
        when(repository.save(unusedCoupon)).thenReturn(unusedCoupon);

        mockMvc.perform(post(couponServiceUrl + "/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent)
                        .characterEncoding(StandardCharsets.UTF_8.name()))
                .andExpect(status().isOk());
    }

    @Test
    void usageSuccessful() throws Exception {
        when(repository.findCouponByUuidAndLocale(eq("UUID-TEST-OK"), eq("US"))).thenReturn(Optional.ofNullable(unusedCoupon));
        when(repository.save(any(Coupon.class))).thenReturn(unusedCoupon);
        CouponUsageRecord usageRecord = new CouponUsageRecord("UUID-TEST-OK", "Max", 10);
        String jsonContent = objectMapper.writeValueAsString(usageRecord);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.patch(couponServiceUrl + "/use")
                .contentType(MediaType.APPLICATION_JSON)
                .with(remoteHost(usIpAddress))
                .content(jsonContent)
                .characterEncoding(StandardCharsets.UTF_8.name())
        ).andExpect(status().isOk()).andReturn();

        verify(repository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getValue().getCurrUse()).isEqualTo(10);

        String contentAsString = mvcResult.getResponse().getContentAsString();
        CouponUsageRecordResponse couponUsageRecord = objectMapper.readValue(contentAsString, CouponUsageRecordResponse.class);
        assertThat(couponUsageRecord.getUsageCount()).isEqualTo(10);
    }

    @Test
    void usageUnsuccessful_wrongLocale() throws Exception {
        when(repository.findCouponByUuidAndLocale("UUID-TEST-OK", "US")).thenReturn(
                Optional.ofNullable(unusedCoupon));
        when(repository.save(any(Coupon.class))).thenReturn(unusedCoupon);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.patch(couponServiceUrl + "/use")
                .contentType(MediaType.APPLICATION_JSON)
                .with(remoteHost(plIpAddress))
                .content("""
                        {"uuid":"UUID-TEST-OK", "usageCount":  10}""")
                .characterEncoding(StandardCharsets.UTF_8.name())
        ).andExpect(status().isNotFound()).andReturn();

        verify(repository, never()).save(any());

        String contentAsString = mvcResult.getResponse().getContentAsString();
        CouponUsageRecordResponse couponUsageRecord = new ObjectMapper().readValue(contentAsString, CouponUsageRecordResponse.class);
        assertThat(couponUsageRecord.getMessage()).isEqualTo("Not found");
    }

    @Test
    void usageUnsuccessful_alreadyConsumed() throws Exception {
        when(repository.findCouponByUuidAndLocale("UUID-TEST-CONSUMED", "US"))
                .thenReturn(Optional.ofNullable(consumedCoupon));
        when(repository.save(any(Coupon.class))).thenReturn(consumedCoupon);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.patch(couponServiceUrl + "/use")
                .contentType(MediaType.APPLICATION_JSON)
                .with(remoteHost(usIpAddress))
                .content("""
                        {"uuid":"UUID-TEST-CONSUMED", "usageCount":  10}""")
                .characterEncoding(StandardCharsets.UTF_8.name())
        ).andExpect(status().isBadRequest()).andReturn();

        verify(repository, never()).save(any());

        String contentAsString = mvcResult.getResponse().getContentAsString();
        CouponUsageRecordResponse couponUsageRecord = new ObjectMapper().readValue(contentAsString, CouponUsageRecordResponse.class);
        assertThat(couponUsageRecord.getMessage()).isEqualTo("Already consumed");
    }

    @Test
    void usageUnsuccessful_overConsumptionByRequest() throws Exception {
        when(repository.findCouponByUuidAndLocale("UUID-TEST-OK", "US"))
                .thenReturn(Optional.ofNullable(unusedCoupon));
        when(repository.save(any(Coupon.class))).thenReturn(unusedCoupon);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.patch(couponServiceUrl + "/use")
                .contentType(MediaType.APPLICATION_JSON)
                .with(remoteHost(usIpAddress))
                .content("""
                        {"uuid":"UUID-TEST-OK", "usageCount":  120}""")
                .characterEncoding(StandardCharsets.UTF_8.name())
        ).andExpect(status().isBadRequest()).andReturn();

        verify(repository, never()).save(any());

        String contentAsString = mvcResult.getResponse().getContentAsString();
        CouponUsageRecordResponse couponUsageRecord = new ObjectMapper().readValue(contentAsString, CouponUsageRecordResponse.class);
        assertThat(couponUsageRecord.getMessage()).isEqualTo("Consumption too high");
        assertThat(couponUsageRecord.getUsageCount()).isEqualTo(120);
    }

    @Test
    void usageUnsuccessful_overConsumptionByUtilization() throws Exception {
        when(repository.findCouponByUuidAndLocale("UUID-TEST-OK", "US"))
                .thenReturn(Optional.ofNullable(partiallyConsumedCoupon));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.patch(couponServiceUrl + "/use")
                .contentType(MediaType.APPLICATION_JSON)
                .with(remoteHost(usIpAddress))
                .content("""
                        {"uuid":"UUID-TEST-OK", "usageCount":  20}""")
                .characterEncoding(StandardCharsets.UTF_8.name())
        ).andExpect(status().isBadRequest()).andReturn();

        verify(repository, never()).save(any());

        String contentAsString = mvcResult.getResponse().getContentAsString();
        CouponUsageRecordResponse couponUsageRecord = new ObjectMapper().readValue(contentAsString, CouponUsageRecordResponse.class);
        assertThat(couponUsageRecord.getMessage()).isEqualTo("Resulting consumption too high");
        assertThat(couponUsageRecord.getUsageCount()).isEqualTo(110);
    }

    @Test
    void dropAll() throws Exception {
        doNothing().when(repository).deleteAll();

        mockMvc.perform(delete(couponServiceUrl + "/drop/all"))
                .andExpect(status().isOk());
    }

    private static RequestPostProcessor remoteHost(final String remoteHost) {
        return request -> {
            request.setRemoteAddr(remoteHost);
            return request;
        };
    }
}
