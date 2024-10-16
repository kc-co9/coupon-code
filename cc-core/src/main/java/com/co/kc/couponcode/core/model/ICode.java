package com.co.kc.couponcode.core.model;

/**
 * The class ICode is used to represent the output of code pool
 *
 * @author kc
 */
public interface ICode {

    /**
     * Get the unique serial number
     *
     * @return The unique serial number
     */
    long getNo();

    /**
     * Get code
     *
     * @return code
     */
    long getCode();

    /**
     * Get format
     *
     * @return The format of code
     */
    String getFormat();

}
