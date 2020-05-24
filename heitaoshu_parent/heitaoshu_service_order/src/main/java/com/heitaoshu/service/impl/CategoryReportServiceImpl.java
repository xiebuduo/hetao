package com.heitaoshu.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.heitaoshu.dao.CategoryReportMapper;
import com.heitaoshu.pojo.order.CategoryReport;
import com.heitaoshu.service.order.CategoryReportService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class CategoryReportServiceImpl implements CategoryReportService {
    @Autowired
    private CategoryReportMapper categoryReportMapper;

    public List<CategoryReport> getYesterdayReport(LocalDate date) {
        return categoryReportMapper.getCategoryReport(date);
    }
    //生成报表

    public void setYesterdayReport(LocalDate date) {
        List<CategoryReport> categoryReports = categoryReportMapper.getCategoryReport(date);
        for (CategoryReport categoryReport : categoryReports) {
            categoryReportMapper.insert(categoryReport);
        }
    }
    //指定范围

    public List<Map> getCategory1Report(String date1, String date2) {
        return categoryReportMapper.getCategory1Report(date1, date2);
    }
}
