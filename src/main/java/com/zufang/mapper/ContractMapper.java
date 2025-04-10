package com.zufang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zufang.entity.Contract;
import com.zufang.entity.ContractTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 合同Mapper接口
 */
@Mapper
public interface ContractMapper extends BaseMapper<Contract> {
    
    /**
     * 根据ID查询合同模板
     * @param id 合同模板ID
     * @return 合同模板
     */
    @Select("SELECT * FROM contract_template WHERE id = #{id}")
    ContractTemplate getContractTemplateById(@Param("id") Long id);
} 