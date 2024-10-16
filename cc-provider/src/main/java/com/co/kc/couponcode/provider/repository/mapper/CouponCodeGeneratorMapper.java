package com.co.kc.couponcode.provider.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.co.kc.couponcode.provider.repository.entities.CouponCodeGenerator;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author kc
 */
@Mapper
public interface CouponCodeGeneratorMapper extends BaseMapper<CouponCodeGenerator> {

    /**
     * 获取最大的NO
     *
     * @return 返回MAX NO
     */
    @Select("SELECT MAX(`no`) FROM coupon_code_generator")
    Long selectMaxNo();

    /**
     * 更新code gen state
     *
     * @param no     No
     * @param xn     Xn
     * @param delta  递增量
     * @param status 状态
     * @return 更新数量
     */
    @Update("UPDATE `coupon_code_generator` SET `xn` = #{xn}, `cnt` = `cnt` + #{delta}, `status` = #{status} WHERE no = #{no}")
    int updateCodeGenByNo(@Param("no") Long no, @Param("xn") Long xn, @Param("delta") Long delta, @Param("status") Integer status);
}
