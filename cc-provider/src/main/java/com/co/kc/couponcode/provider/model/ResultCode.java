package com.co.kc.couponcode.provider.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author kc
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /**
     * 响应CODE
     */
    SUCCESS(0, "成功"),

    AUTH_FAIL(401, "认证失败"),
    AUTH_DENY(402, "权限不足"),
    NOT_EXISTS(404, "数据不存在"),

    ERROR(500, "系统繁忙"),
    NETWORK_ERROR(501, "网络异常"),
    PARAMS_ERROR(502, "参数错误"),
    SIGN_ERROR(503, "签名错误"),
    REPEATED_ERROR(504, "重复提交"),
    TIMEOUT_ERROR(505, "请求超时"),
    OPERATE_ERROR(506, "操作错误"),

    BUSINESS_ERROR(10000, "业务异常");

    private final Integer code;
    private final String msg;
}
