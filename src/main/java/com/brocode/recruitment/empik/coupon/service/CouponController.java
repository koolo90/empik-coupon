package com.brocode.recruitment.empik.coupon.service;

import com.brocode.recruitment.empik.coupon.model.Coupon;
import com.brocode.recruitment.empik.coupon.repository.CouponRepository;
import com.brocode.recruitment.empik.coupon.transport.CouponUsageRecord;
import com.brocode.recruitment.empik.coupon.transport.CouponUsageRecordResponse;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

@RestController
@RequestMapping("/coupon")
@Slf4j
public class CouponController {
    final CouponRepository couponRepository;
    final DatabaseReader databaseReader;

    public CouponController(CouponRepository couponRepository, DatabaseReader databaseReader) {
        this.couponRepository = couponRepository;
        this.databaseReader = databaseReader;
    }

    @PostMapping("/new") @Transactional
    public ResponseEntity<CouponUsageRecordResponse> create(@RequestBody Coupon coupon) {
        Coupon save = this.couponRepository.save(coupon);
        CouponUsageRecordResponse couponUsageRecordResponse = new CouponUsageRecordResponse(save);
        return ResponseEntity.of(Optional.of(couponUsageRecordResponse));
    }

    @PatchMapping("/use")
    public ResponseEntity<CouponUsageRecordResponse> useCoupon(
            @RequestBody CouponUsageRecord couponUsageRecord, HttpServletRequest request) {
        log.info("Utilizing: couponUsageRecord={}", couponUsageRecord);

        CouponUsageRecordResponse.CouponUsageRecordResponseBuilder response = CouponUsageRecordResponse.builder();
        response.uuid(couponUsageRecord.getUuid());

        String isoCountryFromIpAddress = null;
        try {
            isoCountryFromIpAddress = getISOCountryFromIpAddress2(request.getRemoteAddr());
        } catch (IOException | GeoIp2Exception e) {
            response.usageCount(couponUsageRecord.getUsageCount())
                    .message(e.getMessage())
                    .stack(e.getStackTrace());
            return ResponseEntity.badRequest().body(response.build());
        }

        log.info("Utilizing: request comes from Country={}", isoCountryFromIpAddress);

        Optional<Coupon> couponByUUID = this.couponRepository.findCouponByUuidAndLocale(
                couponUsageRecord.getUuid(),
                isoCountryFromIpAddress);
        if (couponByUUID.isEmpty()) {
            response.usageCount(couponUsageRecord.getUsageCount())
                    .message("Not found");
            return new ResponseEntity<>(response.build(), HttpStatus.NOT_FOUND);
        }
        Coupon coupon = couponByUUID.get();
        int currentUseCount = coupon.getCurrUse();
        Integer maximumUseCount = coupon.getMaxUse();
        //validation
        if (currentUseCount >= maximumUseCount) {
            response.usageCount(couponUsageRecord.getUsageCount())
                    .message("Already consumed");
            return ResponseEntity.badRequest().body(response.build());
        }
        if (couponUsageRecord.getUsageCount() >= maximumUseCount) {
            response.usageCount(couponUsageRecord.getUsageCount())
                    .message("Consumption too high");
            return ResponseEntity.badRequest().body(response.build());
        }
        if (currentUseCount + couponUsageRecord.getUsageCount() >= maximumUseCount) {
            response.usageCount(currentUseCount + couponUsageRecord.getUsageCount())
                    .message("Resulting consumption too high");
            return ResponseEntity.badRequest().body(response.build());
        }
        coupon.setCurrUse(currentUseCount + couponUsageRecord.getUsageCount());
        Coupon save = this.couponRepository.save(coupon);
        CouponUsageRecord updatedCouponUsageRecord = new CouponUsageRecord(save.getUuid(), couponUsageRecord.getUser(), save.getCurrUse());
        return ResponseEntity.ok(response.usageCount(updatedCouponUsageRecord.getUsageCount()).build());
    }

    @DeleteMapping("/drop/all")
    public ResponseEntity<CouponUsageRecordResponse> dropAll() {
        this.couponRepository.deleteAll();
        return ResponseEntity.ok(CouponUsageRecordResponse.builder().build());
    }

    private String getISOCountryFromIpAddress2(String ipAddress) throws IOException, GeoIp2Exception {
        return databaseReader.country(InetAddress.getByName(ipAddress)).getCountry().getIsoCode();
    }
}
