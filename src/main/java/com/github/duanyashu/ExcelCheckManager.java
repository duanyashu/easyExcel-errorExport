package com.github.duanyashu;

import java.util.List;

/**
 * @description: excel自主校验接口
 * @author: duanyashu
 * @time: 2020-07-10 09:16
 */
public interface  ExcelCheckManager<T> {

    /**
     * @description: 校验方法
     * @param objects 读取到的数据
     */
    ExcelCheckResult checkImportExcel(List<T> objects);
}
