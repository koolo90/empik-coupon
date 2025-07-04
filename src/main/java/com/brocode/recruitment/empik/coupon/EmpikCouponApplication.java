package com.brocode.recruitment.empik.coupon;

import com.maxmind.geoip2.DatabaseReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

@SpringBootApplication @Slf4j
public class EmpikCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmpikCouponApplication.class, args);
    }

    @Bean
    DatabaseReader databaseReader() {
        File database;
        DatabaseReader dbReader = null;
        try {
            URL url = this.getClass().getClassLoader().getResource("GeoLite2-Country.mmdb");
            database = new File(url.toURI());
            dbReader = new DatabaseReader.Builder(database).build();
        } catch (URISyntaxException e) {
            log.error("Wrong db uri: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("I/O Exception: " + e.getMessage(), e);
        }
        return dbReader; // Retrieve ISO country code
    }
}
