package com.co.kc.couponcode.provider.config;

import com.co.kc.couponcode.provider.persistence.DefaultCodeGen;
import com.co.kc.couponcode.core.CouponCodePool;
import com.co.kc.couponcode.core.algo.LcgFactor;

import com.co.kc.couponcode.core.persistence.ICodeGen;
import com.co.kc.couponcode.provider.repository.dao.CouponCodeGeneratorRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author kc
 */
@Configuration
public class PoolConfig {

    @Bean(initMethod = "init", destroyMethod = "destroy")
    public CouponCodePool couponCodePool(CouponCodeGeneratorRepository couponCodeGeneratorRepository) {
        ICodeGen codeGen = new DefaultCodeGen(LcgFactor.PERIOD_8589934592, couponCodeGeneratorRepository);
        return new CouponCodePool(codeGen);
    }
}
