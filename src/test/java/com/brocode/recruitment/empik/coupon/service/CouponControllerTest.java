package com.brocode.recruitment.empik.coupon.service;

import com.brocode.recruitment.empik.coupon.model.Coupon;
import com.brocode.recruitment.empik.coupon.model.Redemption;
import com.brocode.recruitment.empik.coupon.repository.CouponRepository;
import com.brocode.recruitment.empik.coupon.repository.RedemptionRepository;
import com.brocode.recruitment.empik.coupon.transport.CouponCreationRequest;
import com.brocode.recruitment.empik.coupon.transport.CouponCreationResponse;
import com.brocode.recruitment.empik.coupon.transport.RedemptionRequest;
import com.brocode.recruitment.empik.coupon.transport.RedemptionResponse;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.Country;
import jakarta.persistence.Table;
import jakarta.servlet.http.HttpServletRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Table
class CouponControllerTest {
    @InjectMocks CouponController couponController;
    @Mock CouponRepository couponRepository;
    @Mock RedemptionRepository redemptionRepository;
    @Mock DatabaseReader databaseReader;
    @Mock CouponCreationRequest couponCreationRequest;
    @Mock Coupon persistedCouponMock;
    @Mock RedemptionRequest redemptionRequest;
    @Mock HttpServletRequest request;
    @Mock CountryResponse countryResponse;
    @Mock Country country;
    @Mock Redemption redemption;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void saveCoupon() {
        when(couponRepository.save(any(Coupon.class))).thenReturn(persistedCouponMock);

        ResponseEntity<CouponCreationResponse> couponCreationResponseResponseEntity = couponController.create(couponCreationRequest);

        verify(couponRepository, times(1)).save(any(Coupon.class));
        assertNotNull(couponCreationResponseResponseEntity);
        Assertions.assertThat(couponCreationResponseResponseEntity.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void deleteAllCoupons() {
        ResponseEntity responseEntity = couponController.dropAll();

        verify(couponRepository, times(1)).deleteAll();
        assertNotNull(responseEntity);
        Assertions.assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void consumeCoupon() throws IOException, GeoIp2Exception {
        when(redemptionRequest.getCouponUuid()).thenReturn("MOCK");
        when(redemptionRequest.getUsageCount()).thenReturn(1);
        when(redemptionRequest.isLocalize()).thenReturn(true);
        when(persistedCouponMock.getMaxUse()).thenReturn(3);
        when(country.getIsoCode()).thenReturn("MOCK");
        when(countryResponse.getCountry()).thenReturn(country);
        when(databaseReader.country(any(InetAddress.class))).thenReturn(countryResponse);
        when(couponRepository.findCouponByUuidAndLocaleAndCreationDateBefore(
                anyString(), anyString(), any(LocalDateTime.class))).thenReturn(Optional.of(persistedCouponMock));
        when(redemption.getAmount()).thenReturn(1);
        when(redemptionRepository.findAllByCouponId(any())).thenReturn(List.of(redemption, redemption));

        ResponseEntity use = couponController.use(redemptionRequest, request);

        verify(request, times(1)).getRemoteAddr();
        verify(databaseReader, times(1)).country(any(InetAddress.class));
        verify(couponRepository, times(1)).findCouponByUuidAndLocaleAndCreationDateBefore(anyString(), anyString(), any(LocalDateTime.class));
        verify(redemptionRepository, times(1)).findAllByCouponId(any());
        verify(redemptionRepository, times(1)).save(any(Redemption.class));
        Assertions.assertThat(use).isNotNull();
        Assertions.assertThat(use.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void cannotConsumeCouponDueToIncorrectData() throws IOException, GeoIp2Exception {
        when(redemptionRequest.getCouponUuid()).thenReturn("MOCK");
        when(redemptionRequest.isLocalize()).thenReturn(true);
        when(country.getIsoCode()).thenReturn("MOCK");
        when(countryResponse.getCountry()).thenReturn(country);
        when(databaseReader.country(any(InetAddress.class))).thenReturn(countryResponse);
        when(couponRepository.findCouponByUuidAndLocaleAndCreationDateBefore(
                anyString(), anyString(), any(LocalDateTime.class))).thenReturn(Optional.empty());

        ResponseEntity<RedemptionResponse> use = couponController.use(redemptionRequest, request);

        verify(request, times(1)).getRemoteAddr();
        verify(databaseReader, times(1)).country(any(InetAddress.class));
        verify(couponRepository, times(1)).findCouponByUuidAndLocaleAndCreationDateBefore(anyString(), anyString(), any(LocalDateTime.class));
        verify(redemptionRepository, never()).save(any(Redemption.class));
        Assertions.assertThat(use).isNotNull();
        Assertions.assertThat(use.getStatusCode().is4xxClientError()).isTrue();
        Assertions.assertThat(use.getBody().getErrorMessage()).isEqualTo("Coupon not found!");
    }

    @Test
    void cannotConsumeCouponDueToOveruse() throws IOException, GeoIp2Exception {
        when(redemptionRequest.getCouponUuid()).thenReturn("MOCK");
        when(redemptionRequest.getUsageCount()).thenReturn(1);
        when(redemptionRequest.isLocalize()).thenReturn(true);
        when(persistedCouponMock.getMaxUse()).thenReturn(3);
        when(country.getIsoCode()).thenReturn("MOCK");
        when(countryResponse.getCountry()).thenReturn(country);
        when(databaseReader.country(any(InetAddress.class))).thenReturn(countryResponse);
        when(couponRepository.findCouponByUuidAndLocaleAndCreationDateBefore(
                anyString(), anyString(), any(LocalDateTime.class))).thenReturn(Optional.of(persistedCouponMock));
        when(redemption.getAmount()).thenReturn(1);
        when(redemptionRepository.findAllByCouponId(any())).thenReturn(List.of(redemption, redemption, redemption));

        ResponseEntity<RedemptionResponse> use = couponController.use(redemptionRequest, request);

        verify(request, times(1)).getRemoteAddr();
        verify(databaseReader, times(1)).country(any(InetAddress.class));
        verify(couponRepository, times(1)).findCouponByUuidAndLocaleAndCreationDateBefore(anyString(), anyString(), any(LocalDateTime.class));
        verify(redemptionRepository, times(1)).findAllByCouponId(any());
        verify(redemptionRepository, never()).save(any(Redemption.class));
        Assertions.assertThat(use).isNotNull();
        Assertions.assertThat(use.getStatusCode().is4xxClientError()).isTrue();
        Assertions.assertThat(use.getBody().getErrorMessage()).isEqualTo("Overusage!");
    }

    @Test
    void cannotConsumeCouponDueToFormerUsage() throws IOException, GeoIp2Exception {
        when(redemptionRequest.getCouponUuid()).thenReturn("MOCK");
        when(redemptionRequest.getUsageCount()).thenReturn(1);
        when(redemptionRequest.getUser()).thenReturn("MOCK");
        when(redemptionRequest.isLocalize()).thenReturn(true);
        when(persistedCouponMock.getMaxUse()).thenReturn(3);
        when(country.getIsoCode()).thenReturn("MOCK");
        when(countryResponse.getCountry()).thenReturn(country);
        when(databaseReader.country(any(InetAddress.class))).thenReturn(countryResponse);
        when(couponRepository.findCouponByUuidAndLocaleAndCreationDateBefore(
                anyString(), anyString(), any(LocalDateTime.class))).thenReturn(Optional.of(persistedCouponMock));
        when(redemption.getAmount()).thenReturn(1);
        when(redemption.getHolder()).thenReturn("MOCK");
        when(redemptionRepository.findAllByCouponId(any())).thenReturn(List.of(redemption, redemption));
        when(redemptionRepository.countAllByHolderAndCouponId(anyString(), any())).thenReturn(1L);

        ResponseEntity<RedemptionResponse> use = couponController.use(redemptionRequest, request);

        verify(request, times(1)).getRemoteAddr();
        verify(databaseReader, times(1)).country(any(InetAddress.class));
        verify(couponRepository, times(1)).findCouponByUuidAndLocaleAndCreationDateBefore(anyString(), anyString(), any(LocalDateTime.class));
        verify(redemptionRepository, never()).findAllByCouponId(any());
        verify(redemptionRepository, times(1)).countAllByHolderAndCouponId(anyString(), any());
        verify(redemptionRepository, never()).save(any(Redemption.class));
        Assertions.assertThat(use).isNotNull();
        Assertions.assertThat(use.getStatusCode().is4xxClientError()).isTrue();
        Assertions.assertThat(use.getBody().getErrorMessage()).isEqualTo("Already redeemed!");
    }
}