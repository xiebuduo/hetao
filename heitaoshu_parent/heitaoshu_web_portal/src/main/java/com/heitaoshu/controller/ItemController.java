package com.heitaoshu.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.MapSerializer;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.heitaoshu.pojo.goods.Goods;
import com.heitaoshu.pojo.goods.Sku;
import com.heitaoshu.pojo.goods.Spu;
import com.heitaoshu.service.goods.CategoryService;
import com.heitaoshu.service.goods.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/item")
public class ItemController {

    @Value("${pagePath}")
    private String pagePath;

    @Autowired
    private TemplateEngine templateEngine;

    @Reference
    private SpuService spuService;
    @Reference
    private CategoryService categoryService;

    @GetMapping("/createPage")
    public void createPage(String spuId) throws FileNotFoundException, UnsupportedEncodingException {
        Goods goods = spuService.findGoods(spuId);
        Spu spu = goods.getSpu();
        List<Sku> skuList = goods.getSkuList();
        //添加栏目
        List<String> cateList = new ArrayList<>();
        cateList.add(categoryService.findById(spu.getCategory1Id()).getName());
        cateList.add(categoryService.findById(spu.getCategory2Id()).getName());
        cateList.add(categoryService.findById(spu.getCategory3Id()).getName());
        //得到链接 {'颜色': '银色', '尺码': '150度'}
        Map<String,String> urlMap = new HashMap<>();
        for(Sku urlSku : skuList){
            if("1".equals(urlSku.getStatus())){
                String urlSpec = JSON.toJSONString(JSON.parseObject(urlSku.getSpec()), SerializerFeature.MapSortField);
                System.out.println(urlSpec);
                urlMap.put(urlSpec,urlSku.getId()+".html");
            }
        }

        for(Sku sku : skuList){
            //创建上下文和数据模型
            Context context = new Context();
            Map map = new HashMap();
            map.put("spu",spu);
            map.put("sku",sku);
            map.put("cateList", cateList);

            //添加参数
            Map spuMap = JSON.parseObject(spu.getParaItems());
            map.put("paramItems",spuMap);
            Map skuMap = JSON.parseObject(sku.getSpec());
            map.put("specItems",skuMap);
            //添加规格
            Map<String,List> specMap = (Map)JSON.parseObject(spu.getSpecItems());
            for(String key : specMap.keySet()){
                List<String> specList = specMap.get(key);
                List<Map> mapList = new ArrayList<>();
                for(String value : specList){
                    Map map1 = new HashMap();
                    map1.put("option",value);
                    if(value.equals(skuMap.get(key))){
                        map1.put("checked",true);
                    } else {
                        map1.put("checked",false);
                    }
                    //添加url JSON.parseObject
                    Map<String,String> spec = (Map)JSON.parseObject(sku.getSpec());
                    spec.put(key,value);
                    String specJson = JSON.toJSONString(spec,SerializerFeature.MapSortField);
                    map1.put("url",urlMap.get(specJson));
                    mapList.add(map1);
                }
                specMap.put(key,mapList);
            }

            map.put("specMap",specMap);


            //添加图片
            if(!spu.getImages().trim().equals("")){
                map.put("spuImages",spu.getImages().split(","));
            }else {
                map.put("spuImages",null);
            }
            if(!sku.getImages().trim().equals("")){
                map.put("skuImages",sku.getImages().split(","));
            }else {
                map.put("spuImages",null);
            }

            context.setVariables(map);
            //创建文件

            File path = new File(pagePath);
            if(! path.exists()){
                path.mkdirs();
            }
            File dist = new File(path,sku.getId()+".html");
            //生成文件

            PrintWriter writer = new PrintWriter(dist,"UTF-8");
            templateEngine.process("item",context,writer);
        }
    }
}
