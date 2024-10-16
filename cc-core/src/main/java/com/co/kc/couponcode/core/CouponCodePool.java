package com.co.kc.couponcode.core;

import com.co.kc.couponcode.common.exception.BusinessException;
import com.co.kc.couponcode.common.model.Generator;
import com.co.kc.couponcode.core.algo.Lcg;
import com.co.kc.couponcode.core.model.ICode;
import com.co.kc.couponcode.core.model.IFactor;
import com.co.kc.couponcode.core.persistence.ICodeGen;
import com.google.common.annotations.VisibleForTesting;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static com.co.kc.couponcode.core.CouponCodePool.PoolStatus.*;

/**
 * @author kc
 */
public class CouponCodePool implements Generator<String> {

    private static final Logger LOG = LoggerFactory.getLogger(CouponCodePool.class);

    private static final int DEFAULT_HOT_POOL_SIZE = 100;
    private static final int DEFAULT_COLD_POOL_SIZE = 10000;

    private final ExecutorService hotPoolExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService coldPoolExecutor = Executors.newSingleThreadExecutor();
    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();

    private final BlockingDeque<ICode> coldPool = new LinkedBlockingDeque<>();
    private final BlockingDeque<ICode> hotPool = new LinkedBlockingDeque<>();

    private final ReentrantLock coldPoolTakeLock = new ReentrantLock();
    private final Condition coldPoolNotEmpty = coldPoolTakeLock.newCondition();

    private final ReentrantLock coldPoolPutLock = new ReentrantLock();
    private final Condition coldPoolNotFull = coldPoolPutLock.newCondition();

    private final ReentrantLock hotPoolPutLock = new ReentrantLock();
    private final Condition hotPoolNotFull = hotPoolPutLock.newCondition();

    /**
     * 券池状态
     */
    @Getter
    private PoolStatus status;
    /**
     * COLD券池x0
     */
    @Getter
    private ICode coldX0;
    /**
     * COLD券池xn
     */
    @Getter
    private ICode coldXn;
    /**
     * COLD券池算法Factor
     */
    @Getter
    private IFactor coldFactor;
    /**
     * HOT券池最大大小
     */
    @Getter
    private int maxHotPoolSize;
    /**
     * COLD券池最大大小
     */
    @Getter
    private int maxColdPoolSize;

    @Getter
    private final ICodeGen codeGen;

    public CouponCodePool(ICodeGen codeGen) {
        this(codeGen, DEFAULT_HOT_POOL_SIZE, DEFAULT_COLD_POOL_SIZE);
    }

    public CouponCodePool(ICodeGen codeGen, int maxHotPoolSize, int maxColdPoolSize) {
        this.codeGen = codeGen;
        this.maxHotPoolSize = maxHotPoolSize;
        this.maxColdPoolSize = maxColdPoolSize;
        // update status
        this.status = INIT;
    }

    /**
     * 初始化券池
     */
    public void init() {
        if (INIT.equals(status)) {
            // init ccg
            this.codeGen.init();
            this.coldX0 = codeGen.getX0();
            this.coldXn = codeGen.getXn();
            this.coldFactor = codeGen.getFactor();
            // start schedule
            this.hotPoolExecutor.execute(this::fillHotPool);
            this.coldPoolExecutor.execute(this::fillColdPool);
            this.heartbeatExecutor.scheduleWithFixedDelay(this::reportHeartbeat, 0L, 2L, TimeUnit.SECONDS);
            // update status
            this.status = RUNNING;
        }
    }

    /**
     * 销毁券池
     */
    public void destroy() {
        if (RUNNING.equals(status)) {
            // update status
            this.status = DESTROY;
            // destroy ccg
            this.codeGen.destroy();
            // shutdown schedule
            this.hotPoolExecutor.shutdown();
            this.coldPoolExecutor.shutdown();
            this.heartbeatExecutor.shutdown();
        }
    }

    @Override
    public String next() throws InterruptedException {
        if (!RUNNING.equals(status)) {
            throw new BusinessException("券码池状态异常");
        }
        ICode hotcode = Optional.ofNullable(this.hotPool.poll(1, TimeUnit.SECONDS))
                .orElseThrow(() -> new BusinessException("请求太频繁，请稍后重试"));
        if (this.hotPool.size() < this.maxHotPoolSize) {
            signalHotPoolNotFull();
        }
        String formattedHotcode = String.format(hotcode.getFormat(), hotcode.getCode());
        return hotcode.getNo() + formattedHotcode;
    }

    /**
     * 获取热池大小
     *
     * @return 热池大小
     */
    public int getHotPoolSize() {
        return this.hotPool.size();
    }

    /**
     * 获取冷池大小
     *
     * @return 冷池大小
     */
    public int getColdPoolSize() {
        return this.coldPool.size();
    }

    /**
     * 设置热池最大容量
     *
     * @param maxHotPoolSize 最大容量
     */
    public void setMaxHotPoolSize(int maxHotPoolSize) {
        if (this.maxHotPoolSize < maxHotPoolSize) {
            signalHotPoolNotFull();
        }
        this.maxHotPoolSize = maxHotPoolSize;
    }

    /**
     * 设置冷池最大容量
     *
     * @param maxColdPoolSize 最大容量
     */
    public void setMaxColdPoolSize(int maxColdPoolSize) {
        if (this.maxColdPoolSize < maxColdPoolSize) {
            signalColdPoolNotFull();
        }
        this.maxColdPoolSize = maxColdPoolSize;
    }

    /**
     * 设置X0
     *
     * @param coldX0 x0
     */
    @VisibleForTesting
    /*private*/ void setColdX0(ICode coldX0) {
        this.coldX0 = coldX0;
    }

    /**
     * 设置Xn
     *
     * @param coldXn xn
     */
    @VisibleForTesting
    /*private*/ void setColdXn(ICode coldXn) {
        this.coldXn = coldXn;
    }

    /**
     * 设置LcgHelper
     *
     * @param coldFactor lcgHelper
     */
    @VisibleForTesting
    /*private*/ void setColdFactor(IFactor coldFactor) {
        this.coldFactor = coldFactor;
    }

    /**
     * 设置券码池状态
     *
     * @param status 状态
     */
    @VisibleForTesting
    /*private*/ void setStatus(PoolStatus status) {
        this.status = status;
    }


    /**
     * 获取热池数据
     *
     * @return 热池数据
     */
    public List<ICode> getHotPoolCodeList() {
        return new ArrayList<>(this.hotPool);
    }

    /**
     * 获取冷池数据
     *
     * @return 冷池数据
     */
    public List<ICode> getColdPoolCodeList() {
        return new ArrayList<>(this.coldPool);
    }

    /**
     * 上报心跳
     */
    private void reportHeartbeat() {
        codeGen.keepHeartbeat();
    }

    /**
     * 刷新热池
     */
    @VisibleForTesting
    /* private */ void fillHotPool() {
        for (; INIT.equals(status) || RUNNING.equals(status); ) {
            try {
                if (hotPool.size() >= maxHotPoolSize) {
                    awaitHotPoolNotFull();
                }

                List<ICode> coldCodeList = takeColdPoolCode();

                Map<Long, ICode> codeMap = new HashMap<>();
                Map<Long, Long> deltaMap = new HashMap<>();
                for (ICode code : coldCodeList) {
                    codeMap.put(code.getNo(), code);
                    deltaMap.put(code.getNo(), deltaMap.getOrDefault(code.getNo(), 0L) + 1);
                }
                for (Map.Entry<Long, ICode> codeEntry : codeMap.entrySet()) {
                    Long no = codeEntry.getKey();
                    ICode code = codeEntry.getValue();
                    codeGen.flush(no, code, deltaMap.get(no));
                }
                for (ICode code : coldCodeList) {
                    hotPool.offer(code);
                }
            } catch (InterruptedException e) {
                LOG.error("填充热池发生中断异常", e);
                break;
            } catch (Exception e) {
                LOG.error("填充热池发生异常", e);
            }
        }
    }

    /**
     * 刷新冷池
     */
    @VisibleForTesting
    /* private */ void fillColdPool() {
        for (; INIT.equals(status) || RUNNING.equals(status); ) {
            try {
                if (this.coldPool.size() >= this.maxColdPoolSize) {
                    awaitColdPoolNotFull();
                }

                for (; this.coldPool.size() < this.maxColdPoolSize; ) {
                    coldXn = new PoolCode(coldXn.getNo(), Lcg.next(coldFactor, coldXn.getCode()), coldXn.getFormat());
                    if (coldX0.getNo() == coldXn.getNo()
                            && coldX0.getCode() == coldXn.getCode()
                            && Objects.equals(coldX0.getFormat(), coldXn.getFormat())) {
                        codeGen.select();
                        coldX0 = codeGen.getX0();
                        coldXn = codeGen.getXn();
                        coldFactor = codeGen.getFactor();
                        continue;
                    }

                    this.coldPool.offer(coldXn);
                }

                if (!this.coldPool.isEmpty()) {
                    signalColdPoolNotEmpty();
                }
            } catch (InterruptedException e) {
                LOG.error("填充冷池发生中断异常", e);
                break;
            } catch (Exception e) {
                LOG.error("填充冷池发生异常", e);
            }
        }
    }

    private List<ICode> takeColdPoolCode() throws InterruptedException {
        try {
            if (coldPool.isEmpty()) {
                awaitColdPoolNotEmpty();
            }

            List<ICode> coldCodeList = new LinkedList<>();
            int delta = maxHotPoolSize - hotPool.size();
            for (int i = 0; i < delta && !coldPool.isEmpty(); i++) {
                coldCodeList.add(this.coldPool.poll());
            }

            if (coldPool.size() < maxColdPoolSize) {
                signalColdPoolNotFull();
            }

            return coldCodeList;
        } catch (InterruptedException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("拉取冷池发生异常", e);
            return Collections.emptyList();
        }
    }

    /**
     * 等待冷池券码空缺
     */
    private void awaitColdPoolNotFull() throws InterruptedException {
        coldPoolPutLock.lock();
        try {
            for (; this.coldPool.size() >= this.maxColdPoolSize; ) {
                coldPoolNotFull.await();
            }
        } finally {
            coldPoolPutLock.unlock();
        }
    }

    /**
     * 唤醒冷池填充券码
     */
    private void signalColdPoolNotFull() {
        coldPoolPutLock.lock();
        try {
            coldPoolNotFull.signal();
        } finally {
            coldPoolPutLock.unlock();
        }
    }

    /**
     * 等待冷池券码非空
     */
    private void awaitColdPoolNotEmpty() throws InterruptedException {
        coldPoolTakeLock.lock();
        try {
            for (; coldPool.isEmpty(); ) {
                coldPoolNotEmpty.await();
            }
        } finally {
            coldPoolTakeLock.unlock();
        }
    }

    /**
     * 唤醒冷池获取券码
     */
    private void signalColdPoolNotEmpty() {
        coldPoolTakeLock.lock();
        try {
            coldPoolNotEmpty.signal();
        } finally {
            coldPoolTakeLock.unlock();
        }
    }

    /**
     * 等待热池券码空缺
     */
    private void awaitHotPoolNotFull() throws InterruptedException {
        hotPoolPutLock.lock();
        try {
            for (; hotPool.size() >= maxHotPoolSize; ) {
                hotPoolNotFull.await();
            }
        } finally {
            hotPoolPutLock.unlock();
        }
    }

    /**
     * 唤醒热池填充券码
     */
    private void signalHotPoolNotFull() {
        hotPoolPutLock.lock();
        try {
            hotPoolNotFull.signal();
        } finally {
            hotPoolPutLock.unlock();
        }
    }

    @Getter
    public static class PoolCode implements ICode {
        private final long no;
        private final long code;
        private final String format;

        public PoolCode(long no, long code, String format) {
            this.no = no;
            this.code = code;
            this.format = format;
        }
    }

    /**
     * 券码池状态
     */
    @VisibleForTesting
    enum PoolStatus {
        /**
         * 初始化
         */
        INIT,
        /**
         * 启动
         */
        RUNNING,
        /**
         * 销毁
         */
        DESTROY;
    }

}
