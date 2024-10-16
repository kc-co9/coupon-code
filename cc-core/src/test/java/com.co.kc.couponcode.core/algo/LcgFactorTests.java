package com.co.kc.couponcode.core.algo;

import com.co.kc.couponcode.core.model.IFactor;
import com.co.kc.couponcode.core.utils.MathUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

public class LcgFactorTests {

    /**
     * 测试LcgFactor参数有效性
     */
    @Test
    public void testLcgFactorValidity() {
        Assert.assertTrue("参数A不符合要求", testFactorAValidity());
        Assert.assertTrue("参数C不符合要求", testFactorCValidity());
        Assert.assertTrue("参数M不符合要求", testFactorMValidity());
    }

    /**
     * 测试LcgFactor.PERIOD_1073741824可以生成全周期code
     */
    @Test
    public void testLcgInPeriod1073741824NotRepeated() {
        int cnt = 0;
        BitSet bitSet = new BitSet();
        IFactor factor = LcgFactor.PERIOD_1073741824;
        for (int next = 1; !bitSet.get(next); next = (int) Lcg.next(factor, next)) {
            cnt++;
            bitSet.set(next);
        }
        Assert.assertEquals(1073741824, cnt);
    }

    /**
     * 主要检测 a ≡ 5（mod 8）
     */
    private boolean testFactorAValidity() {
        for (LcgFactor factor : LcgFactor.values()) {
            if (factor.getA() % 8 != 5) {
                return false;
            }
        }
        return true;
    }

    /**
     * 主要检测 m和c互质。
     */
    private boolean testFactorCValidity() {
        for (LcgFactor factor : LcgFactor.values()) {
            if (!MathUtils.areCoprime(factor.getM(), factor.getC())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 主要检测：
     * 1. a-1能被m的所有质因子整除。
     * 2. a-1能被4整除，如果m能被4整除。
     */
    private boolean testFactorMValidity() {
        Set<Long> primesCache = new HashSet<>();
        for (LcgFactor factor : LcgFactor.values()) {
            long a1 = factor.getA() - 1;
            long m = factor.getM();

            // check if a-1 is a multiple of 4 when m is a multiple of 4
            if (m % 4 == 0 && a1 % 4 != 0) {
                return false;
            }

            // check for prime factors
            long sqrtM = (long) Math.sqrt(m);
            for (long num = 2; num <= sqrtM; num++) {
                if (m % num != 0) {
                    continue;
                }
                // check num and its complement
                if (!checkA1ModNumIfNumIsPrime(a1, num, primesCache) || !checkA1ModNumIfNumIsPrime(a1, m / num, primesCache)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkA1ModNumIfNumIsPrime(long a1, long num, Set<Long> primesCache) {
        if (primesCache.contains(num)) {
            return a1 % num == 0;
        } else if (MathUtils.isPrime(num)) {
            primesCache.add(num);
            return a1 % num == 0;
        } else {
            return true;
        }
    }


}
