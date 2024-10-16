package com.co.kc.couponcode.core.algo;

import com.co.kc.couponcode.core.model.IFactor;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 线性同余算法生成器(Linear congruential generator)
 * <p>
 * For m = 2^e and c odd
 *
 * @author kc
 */
@Getter
@AllArgsConstructor
public enum LcgFactor implements IFactor {
    /**
     * the best value in M8 (m, a)
     */
    PERIOD_1073741824(438293613L, 175L, 1L << 30, "%010d"),
    PERIOD_2147483648(37769685L, 173L, 1L << 31, "%010d"),
    PERIOD_4294967296(2891336453L, 175L, 1L << 32, "%010d"),
    PERIOD_8589934592(3766383685L, 173L, 1L << 33, "%010d"),
    PERIOD_17179869184(52765661L, 173L, 1L << 34, "%011d"),
    PERIOD_34359738368(22475205L, 173L, 1L << 35, "%011d"),
    PERIOD_68719476736(12132445L, 173L, 1L << 36, "%011d"),
    PERIOD_1099511627776(330169576829L, 177L, 1L << 40, "%013d"),
    PERIOD_281474976710656(181465474592829L, 171L, 1L << 48, "%015d"),
    PERIOD_1152921504606846976(454339144066433781L, 171L, 1L << 60, "%019d"),
    ;

    private final long a;
    private final long c;
    private final long m;
    private final String format;
}
