package com.co.kc.couponcode.provider.repository.entities;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.co.kc.couponcode.provider.repository.enums.CouponCodeGeneratorStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author kc
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("coupon_code_generator")
public class CouponCodeGenerator extends BaseEntity {
    /**
     * 编号
     */
    @TableField(value = "no")
    private Long no;

    /**
     * multi
     */
    @TableField(value = "a")
    private Long a;

    /**
     * addend
     */
    @TableField(value = "c")
    private Long c;

    /**
     * mod
     */
    @TableField(value = "m")
    private Long m;

    /**
     * x0
     */
    @TableField(value = "x0")
    private Long x0;
    /**
     * xn
     */
    @TableField(value = "xn")
    private Long xn;
    /**
     * xn数量
     */
    @TableField(value = "cnt")
    private Long cnt;
    /**
     * 状态 0-未知 1-待激活 2-激活中 3-已失效
     */
    @TableField(value = "status")
    private CouponCodeGeneratorStatus status;
    /**
     * 心跳时间
     */
    @TableField(value = "heartbeat_at")
    private LocalDateTime heartbeatAt;
}
