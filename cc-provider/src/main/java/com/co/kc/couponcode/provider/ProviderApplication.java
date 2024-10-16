package com.co.kc.couponcode.provider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author K.C
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.co.kc.couponcode.*")
public class ProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }

}
