package com.co.kc.couponcode.provider.repository.dao;

import com.co.kc.couponcode.provider.repository.entities.CouponCodeGenerator;
import com.co.kc.couponcode.provider.repository.enums.CouponCodeGeneratorStatus;
import com.co.kc.couponcode.provider.repository.mapper.CouponCodeGeneratorMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * @author kc
 */
@Repository
public class CouponCodeGeneratorRepository extends BaseRepository<CouponCodeGeneratorMapper, CouponCodeGenerator> {

    public List<CouponCodeGenerator> getInactiveList() {
        return this.list(this.getQueryWrapper()
                .eq(CouponCodeGenerator::getStatus, CouponCodeGeneratorStatus.INACTIVE));
    }

    public List<CouponCodeGenerator> getInactiveUpdateList() {
        return this.list(this.getQueryWrapper()
                .lt(CouponCodeGenerator::getHeartbeatAt, LocalDateTime.now().minus(1, ChronoUnit.MINUTES))
                .eq(CouponCodeGenerator::getStatus, CouponCodeGeneratorStatus.ACTIVATED));
    }

    public long getNextNo() {
        return Optional.ofNullable(this.baseMapper.selectMaxNo()).orElse(0L) + 1;
    }

    public boolean insertGenerator(CouponCodeGenerator generator) {
        return this.save(generator);
    }

    public boolean updateCodeGenByNo(Long no, Long xn, Long delta, Integer status) {
        return this.baseMapper.updateCodeGenByNo(no, xn, delta, status) > 0;
    }

    public boolean updateStatusByIdIfExpectStatus(Long id, CouponCodeGeneratorStatus expected, CouponCodeGeneratorStatus updatedStatus) {
        return this.update(this.getUpdateWrapper()
                .set(CouponCodeGenerator::getStatus, updatedStatus)
                .eq(CouponCodeGenerator::getStatus, expected)
                .eq(CouponCodeGenerator::getId, id));
    }

    public boolean updateStatusByNoIfExpectStatus(Collection<Long> noList, CouponCodeGeneratorStatus expected, CouponCodeGeneratorStatus updatedStatus) {
        if (CollectionUtils.isEmpty(noList)) {
            return true;
        }
        return this.update(this.getUpdateWrapper()
                .set(CouponCodeGenerator::getStatus, updatedStatus)
                .eq(CouponCodeGenerator::getStatus, expected)
                .in(CouponCodeGenerator::getNo, noList));
    }

    public boolean updateStatusByIdIfLossHeartAndMeetExpectStatus(Long id, CouponCodeGeneratorStatus expected, CouponCodeGeneratorStatus updatedStatus) {
        return this.update(this.getUpdateWrapper()
                .set(CouponCodeGenerator::getStatus, updatedStatus)
                .lt(CouponCodeGenerator::getHeartbeatAt, LocalDateTime.now().minus(1, ChronoUnit.MINUTES))
                .eq(CouponCodeGenerator::getStatus, expected)
                .eq(CouponCodeGenerator::getId, id));
    }


    public boolean updateHeartbeatAtById(Long id) {
        return this.update(this.getUpdateWrapper()
                .set(CouponCodeGenerator::getHeartbeatAt, LocalDateTime.now())
                .eq(CouponCodeGenerator::getId, id));
    }

    public boolean updateHeartbeatAtByNo(Collection<Long> noList) {
        if (CollectionUtils.isEmpty(noList)) {
            return true;
        }
        return this.update(this.getUpdateWrapper()
                .set(CouponCodeGenerator::getHeartbeatAt, LocalDateTime.now())
                .in(CouponCodeGenerator::getNo, noList));
    }


}
