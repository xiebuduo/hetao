package com.heitaoshu.service.impl;

import com.heitaoshu.service.goods.CategoryService;
import com.heitaoshu.service.goods.SkuService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Init implements InitializingBean {
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SkuService skuService;

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("=====缓存预热=====");
        //保存分类
        categoryService.saveCategoryTreeToRedis();
        //保存价格
        skuService.saveAllPriceToRedis();
//        System.out.println("======添加索引=====");
//        importSku.importSkuToElasticsearch();
    }
}
