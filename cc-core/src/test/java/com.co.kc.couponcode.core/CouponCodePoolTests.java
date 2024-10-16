package com.co.kc.couponcode.core;

import com.co.kc.couponcode.common.exception.BusinessException;
import com.co.kc.couponcode.core.algo.Lcg;
import com.co.kc.couponcode.core.model.ICode;
import com.co.kc.couponcode.core.persistence.ICodeGen;
import com.co.kc.couponcode.core.persistence.TestCodeGen;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

public class CouponCodePoolTests {

    private static final ICodeGen CODE_GEN = new TestCodeGen();

    @BeforeClass
    public static void setup() {
        CODE_GEN.init();
    }

    @Test
    public void testFillColdPool() {
        CouponCodePool pool = new CouponCodePool(CODE_GEN);
        pool.setColdX0(CODE_GEN.getX0());
        pool.setColdXn(CODE_GEN.getXn());
        pool.setColdFactor(CODE_GEN.getFactor());
        pool.setMaxColdPoolSize(100);

        Thread.currentThread().interrupt();
        pool.fillColdPool();
        Assert.assertEquals(0, pool.getHotPoolSize());
        Assert.assertEquals(100, pool.getColdPoolSize());

        // Repeat to fill coldPool
        Thread.currentThread().interrupt();
        pool.fillColdPool();
        Assert.assertEquals(0, pool.getHotPoolSize());
        Assert.assertEquals(100, pool.getColdPoolSize());
    }

    @Test
    public void testFillHotPoolIfColdPoolIsEmpty() {
        CouponCodePool pool = new CouponCodePool(CODE_GEN);
        pool.setColdX0(CODE_GEN.getX0());
        pool.setColdXn(CODE_GEN.getXn());
        pool.setColdFactor(CODE_GEN.getFactor());
        pool.setMaxHotPoolSize(100);
        pool.setMaxColdPoolSize(1000);

        Thread.currentThread().interrupt();
        pool.fillHotPool();
        Assert.assertEquals(0, pool.getHotPoolSize());
        Assert.assertEquals(0, pool.getColdPoolSize());

        // Repeat to fill hotPool
        Thread.currentThread().interrupt();
        pool.fillHotPool();
        Assert.assertEquals(0, pool.getHotPoolSize());
        Assert.assertEquals(0, pool.getColdPoolSize());
    }

    @Test
    public void testFillHotPoolIfHotPoolIsFull() {
        CouponCodePool pool = new CouponCodePool(CODE_GEN);
        pool.setColdX0(CODE_GEN.getX0());
        pool.setColdXn(CODE_GEN.getXn());
        pool.setColdFactor(CODE_GEN.getFactor());
        pool.setMaxHotPoolSize(10);
        pool.setMaxColdPoolSize(100);

        Thread.currentThread().interrupt();
        pool.fillColdPool();
        Assert.assertEquals(0, pool.getHotPoolSize());
        Assert.assertEquals(100, pool.getColdPoolSize());

        Thread.currentThread().interrupt();
        pool.fillHotPool();
        Assert.assertEquals(10, pool.getHotPoolSize());
        Assert.assertEquals(90, pool.getColdPoolSize());

        // Repeat to fill hotPool
        Thread.currentThread().interrupt();
        pool.fillHotPool();
        Assert.assertEquals(10, pool.getHotPoolSize());
        Assert.assertEquals(90, pool.getColdPoolSize());
    }

    @Test
    public void testFillHotPoolIfColdPoolIsUsedUp() {
        CouponCodePool pool = new CouponCodePool(CODE_GEN);
        pool.setColdX0(CODE_GEN.getX0());
        pool.setColdXn(CODE_GEN.getXn());
        pool.setColdFactor(CODE_GEN.getFactor());
        pool.setMaxHotPoolSize(100);
        pool.setMaxColdPoolSize(10);

        Thread.currentThread().interrupt();
        pool.fillColdPool();
        Assert.assertEquals(0, pool.getHotPoolSize());
        Assert.assertEquals(10, pool.getColdPoolSize());

        Thread.currentThread().interrupt();
        pool.fillHotPool();
        Assert.assertEquals(10, pool.getHotPoolSize());
        Assert.assertEquals(0, pool.getColdPoolSize());

        // Repeat to fill hotPool
        Thread.currentThread().interrupt();
        pool.fillHotPool();
        Assert.assertEquals(10, pool.getHotPoolSize());
        Assert.assertEquals(0, pool.getColdPoolSize());
    }

    @Test
    public void testFillHotPoolIfEqualColdPool() {
        CouponCodePool pool = new CouponCodePool(CODE_GEN);
        pool.setColdX0(CODE_GEN.getX0());
        pool.setColdXn(CODE_GEN.getXn());
        pool.setColdFactor(CODE_GEN.getFactor());
        pool.setMaxHotPoolSize(100);
        pool.setMaxColdPoolSize(100);

        Thread.currentThread().interrupt();
        pool.fillColdPool();
        Assert.assertEquals(0, pool.getHotPoolSize());
        Assert.assertEquals(100, pool.getColdPoolSize());

        Thread.currentThread().interrupt();
        pool.fillHotPool();
        Assert.assertEquals(100, pool.getHotPoolSize());
        Assert.assertEquals(0, pool.getColdPoolSize());

        // Repeat to fill hotPool
        Thread.currentThread().interrupt();
        pool.fillHotPool();
        Assert.assertEquals(100, pool.getHotPoolSize());
        Assert.assertEquals(0, pool.getColdPoolSize());
    }

    @Test
    public void testFillHotPoolIfMaxSizeChanged() {
        CouponCodePool pool = new CouponCodePool(CODE_GEN);
        pool.setColdX0(CODE_GEN.getX0());
        pool.setColdXn(CODE_GEN.getXn());
        pool.setColdFactor(CODE_GEN.getFactor());
        pool.setMaxHotPoolSize(10);
        pool.setMaxColdPoolSize(100);

        Thread.currentThread().interrupt();
        pool.fillColdPool();
        Assert.assertEquals(0, pool.getHotPoolSize());
        Assert.assertEquals(100, pool.getColdPoolSize());

        Thread.currentThread().interrupt();
        pool.fillHotPool();
        Assert.assertEquals(10, pool.getHotPoolSize());
        Assert.assertEquals(90, pool.getColdPoolSize());

        // change hot pool size
        pool.setMaxHotPoolSize(20);
        Thread.currentThread().interrupt();
        pool.fillHotPool();
        Assert.assertEquals(20, pool.getHotPoolSize());
        Assert.assertEquals(80, pool.getColdPoolSize());

        // change hot pool size
        pool.setMaxHotPoolSize(10);
        Thread.currentThread().interrupt();
        pool.fillHotPool();
        Assert.assertEquals(20, pool.getHotPoolSize());
        Assert.assertEquals(80, pool.getColdPoolSize());
    }

    @Test
    public void testFillColdPoolIfMaxSizeChanged() {
        CouponCodePool pool = new CouponCodePool(CODE_GEN);
        pool.setColdX0(CODE_GEN.getX0());
        pool.setColdXn(CODE_GEN.getXn());
        pool.setColdFactor(CODE_GEN.getFactor());

        // change hot cold size
        pool.setMaxColdPoolSize(10);
        Thread.currentThread().interrupt();
        pool.fillColdPool();
        Assert.assertEquals(0, pool.getHotPoolSize());
        Assert.assertEquals(10, pool.getColdPoolSize());

        // change hot cold size
        pool.setMaxColdPoolSize(100);
        Thread.currentThread().interrupt();
        pool.fillColdPool();
        Assert.assertEquals(0, pool.getHotPoolSize());
        Assert.assertEquals(100, pool.getColdPoolSize());

        // change hot cold size
        pool.setMaxColdPoolSize(10);
        Thread.currentThread().interrupt();
        pool.fillColdPool();
        Assert.assertEquals(0, pool.getHotPoolSize());
        Assert.assertEquals(100, pool.getColdPoolSize());
    }

    @Test
    public void testInvokeNextWithFillHotPoolAndColdPool() throws InterruptedException {
        CouponCodePool pool = new CouponCodePool(CODE_GEN);
        pool.setColdX0(CODE_GEN.getX0());
        pool.setColdXn(CODE_GEN.getXn());
        pool.setColdFactor(CODE_GEN.getFactor());
        pool.setMaxHotPoolSize(10);
        pool.setMaxColdPoolSize(100);

        // make the method of 'next' invoked successfully
        pool.setStatus(CouponCodePool.PoolStatus.RUNNING);

        Thread.currentThread().interrupt();
        pool.fillColdPool();

        for (int k = 1; k <= 10; k++) {
            Thread.currentThread().interrupt();
            pool.fillHotPool();

            // consume 10 cold pool codes
            Assert.assertEquals(10, pool.getHotPoolSize());
            Assert.assertEquals(100 - k * 10, pool.getColdPoolSize());

            // consume all of hot pool
            for (int i = 0; i < 10; i++) {
                pool.next();
            }

            // no change
            Assert.assertEquals(0, pool.getHotPoolSize());
            Assert.assertEquals(100 - k * 10, pool.getColdPoolSize());

            // trigger to throw business exception
            try {
                pool.next();
                Assert.fail();
            } catch (Exception ex) {
                Assert.assertEquals(BusinessException.class, ex.getClass());
            }

            // no change
            Assert.assertEquals(0, pool.getHotPoolSize());
            Assert.assertEquals(100 - k * 10, pool.getColdPoolSize());
        }
    }

    @Test
    public void testCouponCodePoolSelect() {
        ICodeGen testCodeGen = new TestCodeGen();
        testCodeGen.init();

        CouponCodePool pool = new CouponCodePool(testCodeGen);
        pool.setColdX0(testCodeGen.getX0());
        pool.setColdXn(testCodeGen.getXn());
        pool.setColdFactor(testCodeGen.getFactor());
        pool.setMaxHotPoolSize(1000);
        pool.setMaxColdPoolSize(1);

        // cold pool => hot pool
        for (int i = 0; i < 100; i++) {
            Thread.currentThread().interrupt();
            pool.fillColdPool();

            Thread.currentThread().interrupt();
            pool.fillHotPool();

            pool.getCodeGen().select();
            pool.setColdX0(testCodeGen.getX0());
            pool.setColdXn(testCodeGen.getXn());
            pool.setColdFactor(testCodeGen.getFactor());
        }

        // to group hotcode by no
        List<ICode> hotCodeList = pool.getHotPoolCodeList();
        Map<Long, Set<Long>> codeMap = new HashMap<>();
        for (ICode code : hotCodeList) {
            codeMap.computeIfAbsent(code.getNo(), k -> new LinkedHashSet<>()).add(code.getCode());
        }

        // to check if 1 key -> 1 value
        Assert.assertEquals(hotCodeList.size(), codeMap.size());
        Assert.assertEquals(hotCodeList.size(), codeMap.values().stream().map(Set::size).mapToInt(Integer::intValue).sum());
    }

    @Test
    public void testCouponCodePoolSelectIfUseUp() {
        TestCodeGen testCodeGen = new TestCodeGen();
        testCodeGen.init();

        CouponCodePool pool = new CouponCodePool(testCodeGen);
        pool.setColdX0(testCodeGen.getX0());
        pool.setColdXn(testCodeGen.getXn());
        pool.setColdFactor(testCodeGen.getFactor());
        pool.setMaxHotPoolSize(1000);
        pool.setMaxColdPoolSize(1);

        // cold pool => hot pool
        for (int i = 0; i < 100; i++) {
            Thread.currentThread().interrupt();
            pool.fillColdPool();

            // mock the LCG is used up
            ICode coldXn = pool.getColdXn();
            long nextColdXn = Lcg.next(pool.getColdFactor(), coldXn.getCode());
            testCodeGen.getRepository().get(coldXn.getNo()).setX0(nextColdXn);
            pool.setColdX0(new CouponCodePool.PoolCode(coldXn.getNo(), nextColdXn, coldXn.getFormat()));

            // trigger to flush xn, remove it from runningMap and invalid the codeGen because of being used up.
            Thread.currentThread().interrupt();
            pool.fillHotPool();
        }

        // to group hotcode by no
        List<ICode> hotCodeList = pool.getHotPoolCodeList();
        Map<Long, Set<Long>> codeMap = new HashMap<>();
        for (ICode code : hotCodeList) {
            // to check running map
            Assert.assertFalse(testCodeGen.getRunningMap().containsKey(code.getNo()));
            // to check codeGen status
            Assert.assertEquals(
                    TestCodeGen.CouponCodeGeneratorStatus.INVALID, testCodeGen.getRepository().get(code.getNo()).getStatus().get());

            codeMap.computeIfAbsent(code.getNo(), k -> new LinkedHashSet<>()).add(code.getCode());
        }

        // to check if 1 key -> 1 value
        Assert.assertEquals(hotCodeList.size(), codeMap.size());
        Assert.assertEquals(hotCodeList.size(), codeMap.values().stream().map(Set::size).mapToInt(Integer::intValue).sum());
    }

}
