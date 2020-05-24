package com.heitaoshu.dao;

import com.heitaoshu.pojo.goods.Sku;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

public interface SkuMapper extends Mapper<Sku> {
    /**
     * 减库存，加销量
     */
    @Select("update tb_sku set num = num - #{num},sale_num = sale_num + #{num} where id = #{skuId}")
    public void upStockAndSaleNum(@Param("skuId") String skuId, @Param("num") Integer num);



}
