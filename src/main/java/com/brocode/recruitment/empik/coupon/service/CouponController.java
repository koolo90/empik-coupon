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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/coupon")
@Slf4j
public class CouponController {
    @Autowired CouponRepository couponRepository;
    @Autowired RedemptionRepository redemptionRepository;
    @Autowired DatabaseReader databaseReader;

    @PostMapping("/new") @Transactional
    public ResponseEntity<CouponCreationResponse> create(@RequestBody CouponCreationRequest couponCreationRequest) {
        Coupon couponForPersistence = new Coupon(couponCreationRequest);
        Coupon save = this.couponRepository.save(couponForPersistence);
        CouponCreationResponse couponCreationResponse = new CouponCreationResponse(save);
        return ResponseEntity.of(Optional.of(couponCreationResponse));
    }

    @DeleteMapping("/drop/all")
    public ResponseEntity dropAll() {
        this.couponRepository.deleteAll();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/redeem")
    public ResponseEntity<RedemptionResponse> use(@RequestBody RedemptionRequest redemptionRequest, HttpServletRequest request) {
        RedemptionResponse.RedemptionResponseBuilder redemptionResponseBuilder = RedemptionResponse.builder();
        redemptionResponseBuilder.redemptionRequest(redemptionRequest);

        String remoteAddr = request.getRemoteAddr();
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName(remoteAddr);
        } catch (UnknownHostException e) {
            redemptionResponseBuilder.errorMessage(e.getMessage()).stackTrace(e.getStackTrace());
            return new ResponseEntity<>(redemptionResponseBuilder.build(), HttpStatus.NOT_FOUND);
        }
        CountryResponse country;
        try {
            country = databaseReader.country(inetAddress);
        } catch (IOException | GeoIp2Exception e) {
            redemptionResponseBuilder.errorMessage(e.getMessage()).stackTrace(e.getStackTrace());
            return new ResponseEntity<>(redemptionResponseBuilder.build(), HttpStatus.NOT_FOUND);
        }
        String isoCode = country.getCountry().getIsoCode();
        Optional<Coupon> optionalCoupon = couponRepository.findCouponByUuidAndLocaleAndCreationDateBefore(redemptionRequest.getCouponUuid(), isoCode, LocalDateTime.now());
        if(optionalCoupon.isEmpty()) {
            redemptionResponseBuilder.errorMessage("Coupon not found!");
            return new ResponseEntity<>(redemptionResponseBuilder.build(), HttpStatus.NOT_FOUND);
        }
        Coupon coupon = optionalCoupon.get();
        List<Redemption> redemptionsByCoupon = redemptionRepository.findAllByCouponId(coupon.getId());
        int usageSum = redemptionsByCoupon.stream().mapToInt(Redemption::getAmount).sum();
        if((usageSum + redemptionRequest.getUsageCount()) > coupon.getMaxUse()) {
            redemptionResponseBuilder.errorMessage("Overusage!");
            return new ResponseEntity<>(redemptionResponseBuilder.build(), HttpStatus.BAD_REQUEST);
        }
        long l = redemptionRepository.countAllByHolderAndCouponId(redemptionRequest.getUser(), coupon.getId());
        if(l > 0) {
            redemptionResponseBuilder.errorMessage("Already redeemed!");
            return new ResponseEntity<>(redemptionResponseBuilder.build(), HttpStatus.BAD_REQUEST);
        }
        Redemption redemption = new Redemption(redemptionRequest, isoCode, coupon);
        Redemption savedRedemption = redemptionRepository.save(redemption);
        redemptionResponseBuilder
                .persistedRedemption(savedRedemption)
                .build();
        return new ResponseEntity<>(redemptionResponseBuilder.build(), HttpStatus.OK);
    }
}
