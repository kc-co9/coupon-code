package com.co.kc.couponcode.common.model;

/**
 * 生成器接口
 *
 * @param <T> 返回类型
 * @author kc
 */
public interface Generator<T> {
    /**
     * 下一个节点
     *
     * @return T 下一个节点返回值
     * @throws InterruptedException 中断异常
     */
    T next() throws InterruptedException;
}
