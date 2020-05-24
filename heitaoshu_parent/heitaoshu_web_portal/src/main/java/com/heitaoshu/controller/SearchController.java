package com.heitaoshu.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.heitaoshu.service.goods.SkuSearchService;
import com.heitaoshu.util.WebUtil;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class SearchController {
    @Reference
    private SkuSearchService skuSearchService;
    @GetMapping("/search")
    public String searchSku(Model model, @RequestParam Map<String, String> searchMap) throws Exception {

        //中文乱码
        searchMap = WebUtil.convertCharsetToUTF8(searchMap);
        if(!searchMap.isEmpty()){
            //如果不存在页码
            if(!searchMap.containsKey("pageNo")){
                searchMap.put("pageNo","1");
            }

            //添加排序
            if((!searchMap.containsKey("sortName")) || searchMap.get("sortName").isEmpty()){
                searchMap.put("sortName","saleNum");
            }
            if(!searchMap.containsKey("sortOrder")){
                searchMap.put("sortOrder","DESC");
            }

            Map result = skuSearchService.Search(searchMap);

            //当前页
            Long currentPage = Long.parseLong(searchMap.get("pageNo"));
            //如果开始页大于总页数
            if(currentPage > Long.parseLong((result.get("pageCount")).toString())){
                currentPage = Long.parseLong((result.get("pageCount")).toString());
            }
            Integer startPage = currentPage.intValue()-3;
            if(startPage < 1){
                startPage = 1;
            }
            Integer endPage = startPage + 6;
            if(endPage > Integer.parseInt(result.get("pageCount").toString())){
                endPage = Integer.parseInt(result.get("pageCount").toString());
            }
            model.addAttribute("startPage",startPage);
            model.addAttribute("currentPage", currentPage);
            model.addAttribute("endPage",endPage);
            StringBuilder urlString = new StringBuilder("/search.xxx?");
            //将spec换成数组
            for(String key : searchMap.keySet()){
                urlString.append("&"+key+"="+searchMap.get(key));
                model.addAttribute("url",urlString);
                model.addAttribute("result",result);
            }
        }


        model.addAttribute("searchMap",searchMap);
        return "search";
    }
}
