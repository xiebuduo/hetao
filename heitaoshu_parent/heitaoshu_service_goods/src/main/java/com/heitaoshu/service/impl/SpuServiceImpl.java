package com.heitaoshu.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.heitaoshu.dao.*;
import com.heitaoshu.entity.PageResult;
import com.heitaoshu.pojo.goods.*;
import com.heitaoshu.service.goods.AuditService;
import com.heitaoshu.service.goods.SkuService;
import com.heitaoshu.service.goods.SpuService;
import com.heitaoshu.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service(interfaceClass = SpuService.class)
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;

    /**
     * 返回全部记录
     * @return
     */
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Spu> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<Spu> spus = (Page<Spu>) spuMapper.selectAll();
        return new PageResult<Spu>(spus.getTotal(),spus.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<Spu> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return spuMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Spu> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<Spu> spus = (Page<Spu>) spuMapper.selectByExample(example);
        return new PageResult<Spu>(spus.getTotal(),spus.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    public Spu findById(String id) {
        return spuMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     * @param spu
     */
    public void add(Spu spu) {
        spuMapper.insert(spu);
    }

    /**
     * 修改
     * @param spu
     */
    public void update(Spu spu) {
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 删除
     *
     * @param id
     */
    public void delete(String id) {
        Map map = new HashMap();
        map.put("spuId",id);
        List<Sku> skuList = skuService.findList(map);
        //删除缓存中的价格
        for(Sku sku : skuList){
            skuService.deleteSkuFromRedisById(sku.getId());
        }
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //逻辑删除
        if("1".equals(spu.getIsDelete())){
            spuMapper.deleteByPrimaryKey(id);
        }else{//物理删除
            spu.setIsDelete("1");
            spuMapper.updateByPrimaryKeySelective(spu);
        }
    }
    /**
     * 逻辑删除复原
     */

    @Override
    public void resetDelete(String id) {
        Spu spu =new Spu();
        spu.setIsDelete("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }




    /*
    添加商品
     */
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private CategoryBrandMapper categoryBrandMapper;
    @Autowired
    private SkuService skuService;
    @Transactional
    public void saveGoods(Goods goods) {

        Spu spu = goods.getSpu();
        //新增
        if(goods.getSpu().getId() == null){
            spu.setId(idWorker.nextId()+"");
            System.out.println(spu.getName()+spu.getId());
            spuMapper.insert(spu);
        }else{
            Example example = new Example(Sku.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("spuId", spu.getId());
            //删除sku
            skuMapper.deleteByExample(example);
            //修改
            spuMapper.updateByPrimaryKey(spu);
        }




        Date date = new Date();
        //获得分类名
        Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());

        List<Sku> skuList = goods.getSkuList();
        System.out.println(skuList);
        for (Sku sku : skuList){
            if(sku.getId() == null){
                sku.setId(idWorker.nextId()+"");
                sku.setCreateTime(date);
            }
            sku.setSpuId(spu.getId());
            if(sku.getSpec() == null || "".equals(sku.getSpec())){
                sku.setSpec("{}");
            }
            String skuName = spu.getName();
            Map<String, String> skuMap = JSON.parseObject(sku.getSpec(), Map.class);
            for(String k:skuMap.values()){
                skuName +=" " + k;
            }
            sku.setName(skuName);
            sku.setUpdateTime(date);
            sku.setCategoryId(spu.getCategory3Id());
            sku.setCategoryName(category.getName());
            sku.setCommentNum(0);
            sku.setSaleNum(0);


            skuMapper.insert(sku);

            //更新缓存
            skuService.saveSkuByIPToRedis(sku.getId(),sku.getPrice());

            //添加栏目品牌关联
            CategoryBrand categoryBrand = new CategoryBrand();
            categoryBrand.setBrandId(spu.getBrandId());
            categoryBrand.setCategoryId(spu.getCategory3Id());
            if(categoryBrandMapper.selectCount(categoryBrand) == 0){
                categoryBrandMapper.insert(categoryBrand);
            }
        }

    }

    /*
    查找商品
     */

    public Goods findGoods(String id){
        Goods goods = new Goods();
        goods.setSpu(spuMapper.selectByPrimaryKey(id));
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId",id);
        goods.setSkuList(skuMapper.selectByExample(example)) ;
        return goods;
    }

    /**
     * 审核
     */
    @Autowired
    private AuditMapper auditMapper;
    public void audit(String id, String status,String message){
        //修改状态，审核和上架
        Spu spu = new Spu();
        spu.setId(id);
        spu.setStatus(status);
        spuMapper.updateByPrimaryKeySelective(spu);

        //记录审核记录
        Audit audit =new Audit();
        audit.setDate(new Date());
        audit.setStatus(status);
        audit.setUser("谢鹏飞");
        audit.setMessage(message);
        auditMapper.insert(audit);
    }

    /**
     * 下架
     */

    @Override
    public void pull(String id) {
        Spu spu = new Spu();
        spu.setId(id);
        spu.setIsMarketable("0");
        spuMapper.updateByPrimaryKeySelective(spu);
        //记录日志

    }

    /**
     * 上架
     */
    @Override
    public void put(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if(! "1".equals(spu.getStatus())){
            throw new RuntimeException("该商品暂未通过审核！");
        }
        spu.setIsMarketable("1");
        spuMapper.updateByPrimaryKeySelective(spu);

        //日志记录
    }

    /**
     * 批量上架
     */

    @Override
    public int putMany(String[] ids) {
        Spu spu = new Spu();
        spu.setIsMarketable("1");

        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id", Arrays.asList(ids));
        criteria.andEqualTo("isMarketable","0");
        criteria.andEqualTo("status","1");

        int i = spuMapper.updateByExampleSelective(spu, example);
        //添加日志
        return i;
    }


    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 主键
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andLike("id","%"+searchMap.get("id")+"%");
            }
            // 货号
            if(searchMap.get("sn")!=null && !"".equals(searchMap.get("sn"))){
                criteria.andLike("sn","%"+searchMap.get("sn")+"%");
            }
            // SPU名
            if(searchMap.get("name")!=null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
            }
            // 副标题
            if(searchMap.get("caption")!=null && !"".equals(searchMap.get("caption"))){
                criteria.andLike("caption","%"+searchMap.get("caption")+"%");
            }
            // 图片
            if(searchMap.get("image")!=null && !"".equals(searchMap.get("image"))){
                criteria.andLike("image","%"+searchMap.get("image")+"%");
            }
            // 图片列表
            if(searchMap.get("images")!=null && !"".equals(searchMap.get("images"))){
                criteria.andLike("images","%"+searchMap.get("images")+"%");
            }
            // 售后服务
            if(searchMap.get("saleService")!=null && !"".equals(searchMap.get("saleService"))){
                criteria.andLike("saleService","%"+searchMap.get("saleService")+"%");
            }
            // 介绍
            if(searchMap.get("introduction")!=null && !"".equals(searchMap.get("introduction"))){
                criteria.andLike("introduction","%"+searchMap.get("introduction")+"%");
            }
            // 规格列表
            if(searchMap.get("specItems")!=null && !"".equals(searchMap.get("specItems"))){
                criteria.andLike("specItems","%"+searchMap.get("specItems")+"%");
            }
            // 参数列表
            if(searchMap.get("paraItems")!=null && !"".equals(searchMap.get("paraItems"))){
                criteria.andLike("paraItems","%"+searchMap.get("paraItems")+"%");
            }
            // 是否上架
            if(searchMap.get("isMarketable")!=null && !"".equals(searchMap.get("isMarketable"))){
                criteria.andLike("isMarketable","%"+searchMap.get("isMarketable")+"%");
            }
            // 是否启用规格
            if(searchMap.get("isEnableSpec")!=null && !"".equals(searchMap.get("isEnableSpec"))){
                criteria.andLike("isEnableSpec","%"+searchMap.get("isEnableSpec")+"%");
            }
            // 是否删除
            if(searchMap.get("isDelete")!=null && !"".equals(searchMap.get("isDelete"))){
                criteria.andLike("isDelete","%"+searchMap.get("isDelete")+"%");
            }
            // 审核状态
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andLike("status","%"+searchMap.get("status")+"%");
            }

            // 品牌ID
            if(searchMap.get("brandId")!=null ){
                criteria.andEqualTo("brandId",searchMap.get("brandId"));
            }
            // 一级分类
            if(searchMap.get("category1Id")!=null ){
                criteria.andEqualTo("category1Id",searchMap.get("category1Id"));
            }
            // 二级分类
            if(searchMap.get("category2Id")!=null ){
                criteria.andEqualTo("category2Id",searchMap.get("category2Id"));
            }
            // 三级分类
            if(searchMap.get("category3Id")!=null ){
                criteria.andEqualTo("category3Id",searchMap.get("category3Id"));
            }
            // 模板ID
            if(searchMap.get("templateId")!=null ){
                criteria.andEqualTo("templateId",searchMap.get("templateId"));
            }
            // 运费模板id
            if(searchMap.get("freightId")!=null ){
                criteria.andEqualTo("freightId",searchMap.get("freightId"));
            }
            // 销量
            if(searchMap.get("saleNum")!=null ){
                criteria.andEqualTo("saleNum",searchMap.get("saleNum"));
            }
            // 评论数
            if(searchMap.get("commentNum")!=null ){
                criteria.andEqualTo("commentNum",searchMap.get("commentNum"));
            }

        }
        return example;
    }

}
