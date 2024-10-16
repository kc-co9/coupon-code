package com.co.kc.couponcode.provider.repository.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author kc
 */
@Getter
@AllArgsConstructor
public enum CouponCodeGeneratorStatus {
    /**
     * 0-未知 1-待激活 2-激活中 3-已失效
     */
    NONE(0, "未知"),
    INACTIVE(1, "待激活"),
    ACTIVATED(2, "激活中"),
    INVALID(3, "已失效"),
    ;
    @EnumValue
    private final int code;
    private final String desc;

    public static Optional<CouponCodeGeneratorStatus> getEnum(int code) {
        return Arrays.stream(values()).filter(e -> e.getCode() == code).findFirst();
    }
}
