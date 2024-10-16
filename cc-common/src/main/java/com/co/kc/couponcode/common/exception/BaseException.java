package com.co.kc.couponcode.common.exception;

import lombok.Getter;

/**
 * @author kc
 */
@Getter
public class BaseException extends RuntimeException {
    protected final String msg;

    public BaseException(String msg) {
        super(msg);
        this.msg = msg;
    }
}
