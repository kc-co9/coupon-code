package com.co.kc.couponcode.provider.repository.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Optional;

/**
 * @author kc
 */
public class BaseRepository<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> {

    public LambdaQueryWrapper<T> getQueryWrapper() {
        return new LambdaQueryWrapper<>();
    }

    public LambdaUpdateWrapper<T> getUpdateWrapper() {
        return Wrappers.lambdaUpdate();
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatchIgnoreEmpty(Collection<T> entityList) {
        if (CollectionUtils.isEmpty(entityList)) {
            return false;
        }
        return this.saveBatch(entityList);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateBatchIgnoreEmpty(Collection<T> entityList) {
        if (CollectionUtils.isEmpty(entityList)) {
            return false;
        }
        return this.saveOrUpdateBatch(entityList);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean updateBatchByIdIgnoreEmpty(Collection<T> entityList) {
        if (CollectionUtils.isEmpty(entityList)) {
            return false;
        }
        return this.updateBatchById(entityList);
    }

    public Optional<T> getFirst(Wrapper<T> queryWrapper) {
        return this.page(new Page<>(1, 1, false), queryWrapper).getRecords().stream().findFirst();
    }

    public boolean isExist(Wrapper<T> queryWrapper) {
        return this.getFirst(queryWrapper).isPresent();
    }

}
