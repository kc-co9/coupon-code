package com.co.kc.couponcode.core.model;

/**
 * The class IFactor is used to represent the factor of LCG
 *
 * @author kc
 */
public interface IFactor {
    /**
     * Get the multiplier of LCG
     *
     * @return multiplier
     */
    long getA();

    /**
     * Get the addend of LCG
     *
     * @return addend
     */
    long getC();

    /**
     * Get the modulo of LCG
     *
     * @return modulo
     */
    long getM();

    /**
     * Get the format of code
     *
     * @return format
     */
    String getFormat();
}
