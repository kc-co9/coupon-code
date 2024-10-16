package com.co.kc.couponcode.core.algo;

import com.co.kc.couponcode.core.model.IFactor;

import java.math.BigInteger;

/**
 * 线性同余算法(Linear congruential generator)
 *
 * @author kc
 */
public class Lcg {

    private Lcg() {
    }


    /**
     * @param factor The factor of LCG
     * @param xn     Xn
     * @return Xn+1
     */
    public static long next(IFactor factor, long xn) {
        return next(factor.getA(), xn, factor.getC(), factor.getM());
    }

    /**
     * 线性同余算法: Xn+1 = (a*Xn + c) mod m，
     * 其中：n >= 0, 0 < m, 0 <= a < m, 0 <= c < m
     * <p>
     * 根据赫尔-多贝尔(Hull–Dobell)定理，在c != 0的情况下，
     * 如符合以下规则即可达到最大周期m:
     * 1. m和c互质。
     * 2. a-1能被m的所有质因子整除。
     * 3. a-1能被4整除，如果m能被4整除。
     *
     * @param a  multiplier
     * @param xn Xn
     * @param c  addend
     * @param m  modulo
     * @return Xn+1
     */
    public static long next(long a, long xn, long c, long m) {
        // (a * xn + c) % m, 在运算过程中可能会存在字段溢出，因此这里转为BigInteger进行运算
        BigInteger abi = BigInteger.valueOf(a);
        BigInteger xnbi = BigInteger.valueOf(xn);
        BigInteger cbi = BigInteger.valueOf(c);
        BigInteger mbi = BigInteger.valueOf(m);
        return abi.multiply(xnbi).add(cbi).mod(mbi).longValue();
    }

}
