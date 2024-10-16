package com.co.kc.couponcode.provider.controller;

import com.co.kc.couponcode.core.CouponCodePool;
import com.co.kc.couponcode.provider.model.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author kc
 */
@RestController
@Api(tags = "券码路由")
@RequestMapping(value = "/coupon")
public class CouponController {

    @Autowired
    private CouponCodePool couponCodePool;

    @ApiOperation(value = "生成券码")
    @GetMapping(value = "/v1/code/next")
    public Result<String> next() {
        try {
            return Result.success(couponCodePool.next());
        } catch (InterruptedException e) {
            return Result.error();
        }
    }
}
