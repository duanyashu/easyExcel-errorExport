package com.github.duanyashu;

import java.util.Map;

/**
 * @description: excel数据导入错误封装对象
 * @author: duanyashu
 * @time: 2020-07-10 09:15
 */
public class ExcelImportErrDto {

    private Object object;

    private Map<Integer,String> cellMap = null;

    public ExcelImportErrDto(){}

    public ExcelImportErrDto(Object object,Map<Integer,String> cellMap){
        this.object = object;
        this.cellMap = cellMap;
    }

    @Override
    public String toString() {
        return "ExcelImportErrDto{" +
                "object=" + object +
                ", cellMap=" + cellMap +
                '}';
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public Map<Integer, String> getCellMap() {
        return cellMap;
    }

    public void setCellMap(Map<Integer, String> cellMap) {
        this.cellMap = cellMap;
    }
}
