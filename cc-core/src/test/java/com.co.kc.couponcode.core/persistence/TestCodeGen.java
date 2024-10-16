package com.co.kc.couponcode.core.persistence;

import com.co.kc.couponcode.core.CouponCodePool;
import com.co.kc.couponcode.core.algo.Lcg;
import com.co.kc.couponcode.core.algo.LcgFactor;
import com.co.kc.couponcode.core.model.ICode;
import com.co.kc.couponcode.core.model.IFactor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.RandomUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class TestCodeGen implements ICodeGen {

    private final AtomicLong runningNo = new AtomicLong(-1);
    @Getter
    private final Map<Long, CodeStatus> runningMap = new ConcurrentHashMap<>();

    private final AtomicLong repositoryNo = new AtomicLong(1);
    @Getter
    private final ConcurrentHashMap<Long, CodeStatus> repository = new ConcurrentHashMap<>();

    @Override
    public boolean init() {
        for (Map.Entry<Long, CodeStatus> entry : repository.entrySet()) {
            CodeStatus codeStatus = entry.getValue();
            if (codeStatus.getHeartbeatAt().compareTo(LocalDateTime.now().minus(30L, ChronoUnit.SECONDS)) < 0) {
                codeStatus.getStatus().compareAndSet(CouponCodeGeneratorStatus.ACTIVATED, CouponCodeGeneratorStatus.INACTIVE);
            }
        }

        this.select();

        return true;
    }

    @Override
    public boolean select() {
        CodeStatus selectCodeStatus = null;
        for (Map.Entry<Long, CodeStatus> entry : repository.entrySet()) {
            CodeStatus codeStatus = entry.getValue();
            if (codeStatus.getStatus().compareAndSet(CouponCodeGeneratorStatus.INACTIVE, CouponCodeGeneratorStatus.ACTIVATED)) {
                selectCodeStatus = codeStatus;
                break;
            }
        }
        if (selectCodeStatus == null) {
            long no = repositoryNo.getAndIncrement();
            long x0 = RandomUtils.nextLong(1, getFactor().getM());
            selectCodeStatus = new CodeStatus();
            selectCodeStatus.setNo(no);
            selectCodeStatus.setA(getFactor().getA());
            selectCodeStatus.setC(getFactor().getC());
            selectCodeStatus.setM(getFactor().getM());
            selectCodeStatus.setX0(x0);
            selectCodeStatus.setXn(x0);
            selectCodeStatus.setCnt(0L);
            selectCodeStatus.setStatus(new AtomicReference<>(CouponCodeGeneratorStatus.ACTIVATED));
            selectCodeStatus.setHeartbeatAt(LocalDateTime.now());
            repository.put(no, selectCodeStatus);
        }
        runningNo.set(selectCodeStatus.getNo());
        runningMap.put(selectCodeStatus.getNo(), selectCodeStatus);
        return true;
    }

    @Override
    public boolean destroy() {
        if (runningMap.isEmpty()) {
            return true;
        }
        runningNo.set(-1);
        runningMap.clear();
        repository.values().forEach(codeStatus -> codeStatus.getStatus().set(CouponCodeGeneratorStatus.INVALID));
        return true;
    }


    @Override
    public IFactor getFactor() {
        return LcgFactor.PERIOD_2147483648;
    }

    @Override
    public Long getNo() {
        if (this.runningNo.get() == -1 || this.runningMap.isEmpty()) {
            return null;
        }
        CodeStatus runningGenerator = runningMap.get(this.runningNo.get());
        return runningGenerator.getNo();
    }

    @Override
    public ICode getX0() {
        if (this.runningNo.get() == -1 || this.runningMap.isEmpty()) {
            return null;
        }
        CodeStatus runningGenerator = runningMap.get(this.runningNo.get());
        Long no = runningGenerator.getNo();
        Long x0 = runningGenerator.getX0();
        if (no == null || x0 == null) {
            return null;
        }
        return new CouponCodePool.PoolCode(no, x0, getFactor().getFormat());
    }

    @Override
    public ICode getXn() {
        if (this.runningNo.get() == -1 || this.runningMap.isEmpty()) {
            return null;
        }
        CodeStatus runningGenerator = runningMap.get(this.runningNo.get());
        Long no = runningGenerator.getNo();
        Long xn = runningGenerator.getXn();
        if (no == null || xn == null) {
            return null;
        }
        return new CouponCodePool.PoolCode(no, xn, getFactor().getFormat());
    }

    @Override
    public boolean flush(Long no, ICode xn, Long delta) {
        if (!runningMap.containsKey(no)) {
            return false;
        }
        CodeStatus runningGenerator = runningMap.get(no);
        boolean isUsedUp = runningGenerator.getX0() == Lcg.next(getFactor(), xn.getCode());
        if (isUsedUp) {
            runningMap.remove(no);
        }
        CouponCodeGeneratorStatus status = isUsedUp ? CouponCodeGeneratorStatus.INVALID : CouponCodeGeneratorStatus.ACTIVATED;
        CodeStatus codeStatus = this.repository.get(no);
        codeStatus.setXn(xn.getCode());
        codeStatus.setCnt(codeStatus.getCnt() + delta);
        codeStatus.getStatus().set(status);
        return true;
    }

    @Override
    public boolean keepHeartbeat() {
        for (Long no : runningMap.keySet()) {
            repository.get(no).setHeartbeatAt(LocalDateTime.now());
        }
        return true;
    }

    @Data
    public static class CodeStatus {
        /**
         * 编号
         */
        private Long no;
        /**
         * multi
         */
        private Long a;
        /**
         * addend
         */
        private Long c;
        /**
         * mod
         */
        private Long m;
        /**
         * x0
         */
        private Long x0;
        /**
         * xn
         */
        private Long xn;
        /**
         * xn数量
         */
        private Long cnt;
        /**
         * 状态 0-未知 1-待激活 2-激活中 3-已失效
         */
        private AtomicReference<CouponCodeGeneratorStatus> status;
        /**
         * 心跳时间
         */
        private LocalDateTime heartbeatAt;
    }

    @Getter
    @AllArgsConstructor
    public enum CouponCodeGeneratorStatus {
        /**
         * 1-待激活 2-激活中 3-已失效
         */
        INACTIVE(1, "待激活"),
        ACTIVATED(2, "激活中"),
        INVALID(3, "已失效"),
        ;
        private final int code;
        private final String desc;
    }


}
