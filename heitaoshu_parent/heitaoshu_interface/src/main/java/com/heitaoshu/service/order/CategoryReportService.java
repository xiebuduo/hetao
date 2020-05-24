package com.heitaoshu.service.order;

import com.heitaoshu.pojo.order.CategoryReport;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CategoryReportService {
    public List<CategoryReport> getYesterdayReport(LocalDate date);

    //生成报表
    public void setYesterdayReport(LocalDate date);

    //指定范围
    public List<Map> getCategory1Report(String date1, String date2);
}
