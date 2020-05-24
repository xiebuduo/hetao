package com.heitaoshu.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.heitaoshu.dao.BrandMapper;
import com.heitaoshu.dao.SpecMapper;
import com.heitaoshu.service.goods.SkuSearchService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SkuSearchServiceImpl implements SkuSearchService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SpecMapper specMapper;
    public Map Search(Map<String, String> searchMap) {
        //封装查询请求
        SearchRequest searchRequest = new SearchRequest("sku");

        //构建查询源
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            //1.构建布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                //1.1构建匹配查询
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("name", searchMap.get("keywords"));
                //添加匹配查询
        boolQueryBuilder.must(matchQueryBuilder);
                //1.2过滤查询
        if(searchMap.containsKey("categoryName")){
            TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery("categoryName",searchMap.get("category"));
            boolQueryBuilder.filter(termsQueryBuilder);

        }
                //1.3添加品牌
        if(searchMap.containsKey("brand")){
            TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery("brandName",searchMap.get("brand"));
            boolQueryBuilder.filter(termsQueryBuilder);
        }

                //1.4添加规格格
        for(String key : searchMap.keySet()){
            if(key.startsWith("spec.")){
                TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery(key+".keyword",searchMap.get(key));
                boolQueryBuilder.filter(termsQueryBuilder);
            }
        }
                //1.5添加价格
        if (searchMap.containsKey("price")) {
            String[] price = searchMap.get("price").split("-");
            if(!price[0].equals("0")){
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price").gte(price[0]+"00");
                boolQueryBuilder.filter(rangeQueryBuilder);
            }
            if(!price[1].equals("*")){
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price").lte(price[1] + "00");
                boolQueryBuilder.filter(rangeQueryBuilder);
            }
        }
                //1.6分页
        searchSourceBuilder.from(Integer.parseInt(searchMap.get("pageNo")));
        searchSourceBuilder.size(30);

        //添加布尔查询
        searchSourceBuilder.query(boolQueryBuilder);



            //2.聚合查询
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("sku_category").field("categoryName");
        searchSourceBuilder.aggregation(termsAggregationBuilder);

        //排序
            searchSourceBuilder.sort(searchMap.get("sortName"), SortOrder.valueOf(searchMap.get("sortOrder")));
        //高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("name").preTags("<font style='color:red'>").postTags("</font>");
        searchSourceBuilder.highlighter(highlightBuilder);

        //封装
        searchRequest.source(searchSourceBuilder);


        //获取查询结果
        Map returnMap = new HashMap<>();
        List<Map<String, Object>> returnMapList = new ArrayList<>();
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits searchHits = searchResponse.getHits();
            Long total = searchHits.getTotalHits();
            System.out.println("查询总数:" + total);

            //返回查询结果
            SearchHit[] searchHits1 = searchHits.getHits();
            //高亮
            for (SearchHit searchHit : searchHits1) {
                Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
                Map<String, Object> map = searchHit.getSourceAsMap();
                map.put("name",highlightFields.get("name").fragments()[0].toString());
                returnMapList.add(map);
            }

            //商品分类
            Aggregations aggregations = searchResponse.getAggregations();
            Map<String,Aggregation> stringAggregationMap = aggregations.getAsMap();
            Terms terms = (Terms) stringAggregationMap.get("sku_category");
            List<? extends Terms.Bucket> buckets = terms.getBuckets();
            List<String> categoryList = new ArrayList<>();
            for (Terms.Bucket bucket : buckets){
                categoryList.add(bucket.getKeyAsString());
            }

            //添加品牌
            List<Map<String, String>> brandMaps = new ArrayList<>();
            //添加规格
            List<Map> specList = new ArrayList<>();
                //如果没有选择分类
            if(searchMap.get("category") == null){
                if (categoryList.size() > 0) {
                    //品牌
                    brandMaps = brandMapper.findBrandByCategoryName(categoryList.get(0));
                    //规格
                    specList = specMapper.findSpecMapByCategoryName(categoryList.get(0));
                }
            } else { // 选择分类
                brandMaps = brandMapper.findBrandByCategoryName(searchMap.get("category"));
                specList = specMapper.findSpecMapByCategoryName(searchMap.get("category"));
            }
                returnMap.put("brandList",brandMaps);

            // 规格参数转数组
                if(!specList.isEmpty()){
                    for(Map specMap : specList){
                        String[] specParam = specMap.get("options").toString().split(",");
                        specMap.put("options",specParam);
                    }
                }
            if(searchHits.getTotalHits() > 0){
                returnMap.put("pageCount",(searchHits.getTotalHits()%30 == 0 ?searchHits.getTotalHits()/30 : (searchHits.getTotalHits()/30) + 1));
            }
            if(searchHits.getTotalHits() == 0) {
                returnMap.put("pageCount",1);
            }
            returnMap.put("total",searchHits.getTotalHits());
            returnMap.put("specList",specList);
            returnMap.put("rows", returnMapList);
            returnMap.put("category",categoryList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnMap;

    }
}