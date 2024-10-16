CREATE DATABASE IF NOT EXISTS `coupon_code`;

use `coupon_code`;

DROP TABLE IF EXISTS `coupon_code_generator`;
CREATE TABLE `coupon_code_generator`
(
    `id`           BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '主键',
    `no`           BIGINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT '编号',
    `a`            BIGINT           NOT NULL DEFAULT 0 COMMENT 'multiplier',
    `c`            BIGINT           NOT NULL DEFAULT 0 COMMENT 'addend',
    `m`            BIGINT           NOT NULL DEFAULT 0 COMMENT 'modulo',
    `x0`           BIGINT           NOT NULL DEFAULT -1 COMMENT 'x0',
    `xn`           BIGINT           NOT NULL DEFAULT -1 COMMENT 'xn',
    `cnt`          BIGINT UNSIGNED  NOT NULL DEFAULT 0 COMMENT 'xn数量',
    `status`       TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '状态 0-未知 1-待激活 2-激活中 3-已失效',
    `heartbeat_at` DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '心跳时间',
    `created_at`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY uk_no (`no`) USING BTREE
) ENGINE = InnoDB COMMENT ='券码xn生成器';