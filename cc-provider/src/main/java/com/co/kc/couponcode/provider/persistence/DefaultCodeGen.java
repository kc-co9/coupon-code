package com.co.kc.couponcode.provider.persistence;

import com.co.kc.couponcode.core.CouponCodePool;
import com.co.kc.couponcode.core.algo.Lcg;
import com.co.kc.couponcode.core.model.ICode;
import com.co.kc.couponcode.core.model.IFactor;
import com.co.kc.couponcode.core.persistence.ICodeGen;
import com.co.kc.couponcode.provider.repository.dao.CouponCodeGeneratorRepository;
import com.co.kc.couponcode.provider.repository.entities.CouponCodeGenerator;
import com.co.kc.couponcode.provider.repository.enums.CouponCodeGeneratorStatus;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author kc
 */
public class DefaultCodeGen implements ICodeGen {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultCodeGen.class);

    private final IFactor factor;
    private final CouponCodeGeneratorRepository couponCodeGeneratorRepository;

    private final AtomicLong runningNo = new AtomicLong(-1);
    private final Map<Long, CouponCodeGenerator> runningMap = new ConcurrentHashMap<>();

    public DefaultCodeGen(IFactor factor, CouponCodeGeneratorRepository couponCodeGeneratorRepository) {
        this.factor = factor;
        this.couponCodeGeneratorRepository = couponCodeGeneratorRepository;
    }

    @Override
    public boolean init() {
        List<CouponCodeGenerator> inactiveUpdateList = couponCodeGeneratorRepository.getInactiveUpdateList();
        for (CouponCodeGenerator couponCodeGenerator : inactiveUpdateList) {
            couponCodeGeneratorRepository.updateStatusByIdIfLossHeartAndMeetExpectStatus(
                    couponCodeGenerator.getId(), CouponCodeGeneratorStatus.ACTIVATED, CouponCodeGeneratorStatus.INACTIVE);
        }

        // select generator
        this.select();

        return true;
    }

    @Override
    public boolean select() {
        CouponCodeGenerator selectGenerator = null;
        List<CouponCodeGenerator> inactiveList = couponCodeGeneratorRepository.getInactiveList();
        for (CouponCodeGenerator couponCodeGenerator : inactiveList) {
            if (Objects.equals(couponCodeGenerator.getA(), factor.getA())
                    && Objects.equals(couponCodeGenerator.getC(), factor.getC())
                    && Objects.equals(couponCodeGenerator.getM(), factor.getM())) {
                boolean hasBound = true;
                //noinspection ConstantConditions
                hasBound &= couponCodeGeneratorRepository.updateHeartbeatAtById(couponCodeGenerator.getId());
                hasBound &= couponCodeGeneratorRepository.updateStatusByIdIfExpectStatus(
                        couponCodeGenerator.getId(), CouponCodeGeneratorStatus.INACTIVE, CouponCodeGeneratorStatus.ACTIVATED);
                if (hasBound) {
                    selectGenerator = couponCodeGenerator;
                    break;
                }
            }
        }
        if (selectGenerator == null) {
            boolean isInserted;
            do {
                try {
                    long no = couponCodeGeneratorRepository.getNextNo();
                    long x0 = RandomUtils.nextLong(1, factor.getM());
                    selectGenerator = new CouponCodeGenerator();
                    selectGenerator.setNo(no);
                    selectGenerator.setA(factor.getA());
                    selectGenerator.setC(factor.getC());
                    selectGenerator.setM(factor.getM());
                    selectGenerator.setX0(x0);
                    selectGenerator.setXn(x0);
                    selectGenerator.setCnt(0L);
                    selectGenerator.setStatus(CouponCodeGeneratorStatus.ACTIVATED);
                    selectGenerator.setHeartbeatAt(LocalDateTime.now());
                    isInserted = couponCodeGeneratorRepository.insertGenerator(selectGenerator);
                } catch (DuplicateKeyException duplicateKeyException) {
                    LOG.info("服务启动发生并发创建生成器，继续重试...");
                    isInserted = false;
                }
            } while (!isInserted);
        }
        runningNo.set(selectGenerator.getNo());
        runningMap.put(selectGenerator.getNo(), selectGenerator);
        return true;
    }

    @Override
    public boolean destroy() {
        if (runningMap.isEmpty()) {
            return true;
        }
        runningNo.set(-1);
        runningMap.clear();
        couponCodeGeneratorRepository.updateStatusByNoIfExpectStatus(
                runningMap.keySet(), CouponCodeGeneratorStatus.ACTIVATED, CouponCodeGeneratorStatus.INACTIVE);
        return true;
    }

    @Override
    public IFactor getFactor() {
        return this.factor;
    }

    @Override
    public Long getNo() {
        if (this.runningNo.get() == -1 || this.runningMap.isEmpty()) {
            return null;
        }
        CouponCodeGenerator runningGenerator = runningMap.get(this.runningNo.get());
        return runningGenerator.getNo();
    }

    @Override
    public ICode getX0() {
        if (this.runningNo.get() == -1 || this.runningMap.isEmpty()) {
            return null;
        }
        CouponCodeGenerator runningGenerator = runningMap.get(this.runningNo.get());
        Long no = runningGenerator.getNo();
        Long x0 = runningGenerator.getX0();
        if (no == null || x0 == null) {
            return null;
        }
        return new CouponCodePool.PoolCode(no, x0, factor.getFormat());
    }

    @Override
    public ICode getXn() {
        if (this.runningNo.get() == -1 || this.runningMap.isEmpty()) {
            return null;
        }
        CouponCodeGenerator runningGenerator = runningMap.get(this.runningNo.get());
        Long no = runningGenerator.getNo();
        Long xn = runningGenerator.getXn();
        if (no == null || xn == null) {
            return null;
        }
        return new CouponCodePool.PoolCode(no, xn, factor.getFormat());
    }

    @Override
    public boolean flush(Long no, ICode xn, Long delta) {
        if (!runningMap.containsKey(no)) {
            return false;
        }
        CouponCodeGenerator runningGenerator = runningMap.get(no);
        boolean isUsedUp = runningGenerator.getX0() == Lcg.next(factor, xn.getCode());
        if (isUsedUp) {
            runningMap.remove(no);
        }
        CouponCodeGeneratorStatus status = isUsedUp ? CouponCodeGeneratorStatus.INVALID : CouponCodeGeneratorStatus.ACTIVATED;
        return couponCodeGeneratorRepository.updateCodeGenByNo(no, xn.getCode(), delta, status.getCode());
    }

    @Override
    public boolean keepHeartbeat() {
        return couponCodeGeneratorRepository.updateHeartbeatAtByNo(runningMap.keySet());
    }
}
