package cn.tuodayun;

import java.util.HashMap;
import java.util.Map;

/**
 * @description: excel数据导入错误封装对象
 * @author: duanyashu
 * @time: 2020-07-10 09:15
 */
public class ExcelImportErrDto<T> {

    private T object;

    private Map<Integer,String> cellMap = null;

    public ExcelImportErrDto(){}

    public ExcelImportErrDto(T object,Map<Integer,String> cellMap){
        this.object = object;
        this.cellMap = cellMap;
    }

    @Override
    public String toString() {
        return "ExcelImportErrObjectDto{" +
                "object=" + object +
                ", cellMap=" + cellMap +
                '}';
    }

    public Object getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }

    public Map<Integer, String> getCellMap() {
        return cellMap;
    }

    public void setCellMap(Map<Integer, String> cellMap) {
        this.cellMap = cellMap;
    }
}
