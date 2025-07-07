package com.brocode.recruitment.empik.coupon.service;

import com.brocode.recruitment.empik.coupon.model.Coupon;
import com.brocode.recruitment.empik.coupon.model.Redemption;
import com.brocode.recruitment.empik.coupon.repository.CouponRepository;
import com.brocode.recruitment.empik.coupon.repository.RedemptionRepository;
import com.brocode.recruitment.empik.coupon.transport.*;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/coupon")
@Slf4j
public class CouponController {
    final CouponRepository couponRepository;
    final RedemptionRepository redemptionRepository;
    final DatabaseReader databaseReader;

    public CouponController(CouponRepository couponRepository, RedemptionRepository redemptionRepository, DatabaseReader databaseReader) {
        this.couponRepository = couponRepository;
        this.redemptionRepository = redemptionRepository;
        this.databaseReader = databaseReader;
    }

    @PostMapping("/new") @Transactional
    public ResponseEntity<CouponCreationResponse> create(@Valid @RequestBody CouponCreationRequest couponCreationRequest) {
        log.info("Creating coupon {}", couponCreationRequest);
        Coupon couponForPersistence = new Coupon(couponCreationRequest);
        Coupon save = this.couponRepository.save(couponForPersistence);
        CouponCreationResponse couponCreationResponse = new CouponCreationResponse(save);
        log.info("Coupon {} created", couponCreationRequest);
        return ResponseEntity.of(Optional.of(couponCreationResponse));
    }

    @DeleteMapping("/drop/all")
    public ResponseEntity<CouponDeletionResponse> dropAll() {
        log.info("Dropping all coupons");
        this.couponRepository.deleteAll();
        CouponDeletionResponse couponCreationResponse = CouponDeletionResponse.builder().message("All coupons deleted").build();
        log.info("All coupons deleted");
        return ResponseEntity.ok().body(couponCreationResponse);
    }

    @PostMapping("/redeem")
    public ResponseEntity<RedemptionResponse> use(@Valid @RequestBody RedemptionRequest redemptionRequest, HttpServletRequest request) {
        log.info("Using coupon on request {}", redemptionRequest);
        RedemptionResponse.RedemptionResponseBuilder redemptionResponseBuilder = RedemptionResponse.builder();
        redemptionResponseBuilder.redemptionRequest(redemptionRequest).success(true);

        String isoCode = null;
        try {
            isoCode = resolveIcoCountryCode(request, redemptionRequest.isLocalize());
        } catch (IOException | GeoIp2Exception e) {
            redemptionResponseBuilder.errorMessage(e.getMessage()).stackTrace(e.getStackTrace());
            return new ResponseEntity<>(redemptionResponseBuilder.build(), HttpStatus.NOT_FOUND);
        }

        log.info("Fetching coupon from database");
        Optional<Coupon> optionalCoupon = couponRepository.findCouponByUuidIgnoreCaseAndLocaleAndCreationDateBefore(redemptionRequest.getCouponUuid(), isoCode, LocalDateTime.now());
        String couponId = optionalCoupon.map(Coupon::getUuid).orElse("");
        Integer maxUse = optionalCoupon.map(Coupon::getMaxUse).orElse(0);
        if(couponId.isEmpty()) {
            log.warn("Coupon not found");
            redemptionResponseBuilder.errorMessage("Coupon not found!");
            return new ResponseEntity<>(redemptionResponseBuilder.build(), HttpStatus.NOT_FOUND);
        }
        log.info("Fetched coupon from database");

        log.info("Checking if user is legitimate for coupon");
        long usageCount = redemptionRepository.countAllByHolderAndCouponUuidIgnoreCase(redemptionRequest.getUser(), couponId);
        if(usageCount > 0) {
            log.warn("User cannot consume coupon due to former use");
            redemptionResponseBuilder.errorMessage("User already redeemed coupon!");
            return new ResponseEntity<>(redemptionResponseBuilder.build(), HttpStatus.NOT_ACCEPTABLE);
        }

        log.info("Checking if coupon will not be overused");
        List<Redemption> redemptionsByCoupon = redemptionRepository.findAllByCouponUuidIgnoreCase(couponId);
        int usageSum = redemptionsByCoupon.stream().mapToInt(Redemption::getAmount).sum();
        if((usageSum + redemptionRequest.getUsageCount()) > maxUse) {
            log.warn("Overusage detected");
            redemptionResponseBuilder.errorMessage("Overusage!");
            return new ResponseEntity<>(redemptionResponseBuilder.build(), HttpStatus.NOT_ACCEPTABLE);
        }

        log.info("Persisting information about usage consumption");
        Redemption redemption = new Redemption(redemptionRequest, couponId, isoCode);
        Redemption savedRedemption = redemptionRepository.save(redemption);
        redemptionResponseBuilder.persistedRedemption(savedRedemption).build();
        log.info("Redemption successful!");
        return new ResponseEntity<>(redemptionResponseBuilder.build(), HttpStatus.OK);
    }

    private String resolveIcoCountryCode(HttpServletRequest request, boolean localize) throws IOException, GeoIp2Exception {
        String isoCode = "US";
        if (!localize) {
            log.warn("Resolving ignored basing on request, that should not happen in PROD env.");
            return isoCode;
        }

        log.info("Resolving coupon consumption location from request{}", request);
        String remoteAddr = request.getRemoteAddr();
        InetAddress inetAddress;
        inetAddress = InetAddress.getByName(remoteAddr);
        CountryResponse country = databaseReader.country(inetAddress);
        log.info("Localization resolved");
        return country.getCountry().getIsoCode();
    }
}
