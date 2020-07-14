package com.github.duanyashu.listener;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelAnalysisException;
import com.alibaba.excel.util.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.duanyashu.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:easyExcel监听器
 * @author: duanyashu
 * @time: 2020-07-10 09:13
 */
public class EasyExcelListener <T>  extends AnalysisEventListener<T> {

    /**
     * 成功数据结果集
     */
    private List<T> successList = new ArrayList<>();

    /**
     * 错误数据结果集
     */
    private List<ExcelImportErrDto> errList = new ArrayList<>();

    //处理逻辑service
    private ExcelCheckManager excelCheckManager;

    /**
     * 每隔5条存储数据库，实际使用中可以3000条，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 5;

    /**
     * 存放解析的临时对象
     */
    private List<T> list = new ArrayList<>();

    /**
     *excel对应的实体对象的反射类
     */
    private Class<T> clazz;

    /**
     * 是否处理导入错误数据
     */
    private boolean isErrorExport = true;

    private HttpServletResponse response;

    public EasyExcelListener(HttpServletResponse response,ExcelCheckManager excelCheckManager,Class clazz,boolean isErrorExport){
        this.excelCheckManager = excelCheckManager;
        this.clazz = clazz;
        this.isErrorExport=isErrorExport;
        this.response = response;
    }
    public EasyExcelListener(HttpServletResponse response,ExcelCheckManager excelCheckManager,Class clazz){
        this.excelCheckManager = excelCheckManager;
        this.clazz = clazz;
        this.response=response;
    }
    /**
     * 这个每一条数据解析都会来调用
     *
     * @param t
     *            one row value. Is is same as {@link AnalysisContext#readRowHolder()}
     * @param analysisContext
     */
    @Override
    public void invoke(T t, AnalysisContext analysisContext) {
        String errMsg;
        Map<Integer, String> resultMap = null;
        try {
            //根据excel数据实体中的javax.validation + 正则表达式来校验excel数据
            resultMap = EasyExcelValiHelper.validateEntity(t);
        } catch (NoSuchFieldException e) {
            throw new ExcelAnalysisException("第"+analysisContext.readRowHolder().getRowIndex()+"行解析数据出错");
        }
        if (resultMap!=null){
            ExcelImportErrDto excelImportErrObjectDto = new ExcelImportErrDto(t, resultMap);
            errList.add(excelImportErrObjectDto);
        }else{
            list.add(t);
        }
        //每1000条处理一次
        if (list.size() >= BATCH_COUNT){
            //自主校验
            if (excelCheckManager!=null){
                ExcelCheckResult result = excelCheckManager.checkImportExcel(list);
                successList.addAll(result.getSuccessDtos());
                errList.addAll(result.getErrDtos());
            }
            list.clear();
        }
    }

    /**
     * 所有数据解析完成了 都会来调用
     * */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (excelCheckManager!=null) {
            ExcelCheckResult result = excelCheckManager.checkImportExcel(list);
            successList.addAll(result.getSuccessDtos());
            errList.addAll(result.getErrDtos());
        }
        list.clear();
        if (isErrorExport){
            try {
                exportErrorExcel();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * @description: 校验excel头部格式，必须完全匹配
     * @param headMap 传入excel的头部（第一行数据）数据的index,name
     */
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        super.invokeHeadMap(headMap, context);
        if (clazz != null){
            try {
                Map<Integer, String> indexNameMap = getIndexNameMap(clazz);
                Set<Integer> keySet = indexNameMap.keySet();
                for (Integer key : keySet) {
                    if (StringUtils.isEmpty(headMap.get(key))){
                        throw new ExcelAnalysisException("解析excel出错，请传入正确格式的excel");
                    }
                    if (!headMap.get(key).equals(indexNameMap.get(key))){
                        throw new ExcelAnalysisException("解析excel出错，请传入正确格式的excel");
                    }
                }

            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @description: 获取注解里ExcelProperty的value，用作校验excel
     * @param clazz
     * @throws
     * @return 错误数据的坐标和提示  （eg:0-不能为空）
     */
    public Map<Integer,String> getIndexNameMap(Class clazz) throws NoSuchFieldException {
        Map<Integer,String> result = new HashMap<>();
        Field field;
        Field[] fields=clazz.getDeclaredFields();
        for (int i = 0; i <fields.length ; i++) {
            field=clazz.getDeclaredField(fields[i].getName());
            field.setAccessible(true);
            ExcelProperty excelProperty=field.getAnnotation(ExcelProperty.class);
            if(excelProperty!=null){
                int index = excelProperty.index();
                index = index==-1? i :index;
                String[] values = excelProperty.value();
                StringBuilder value = new StringBuilder();
                for (String v : values) {
                    value.append(v);
                }
                result.put(index,value.toString());
            }
        }
        return result;
    }

    /**
     * 获取错误数据
     * @return
     */
    public List<ExcelImportErrDto> getErrList(){
        return errList;
    }

    /**
     * 获取正确数据
     * @return
     */
    public List<T> getSuccessList(){
        return successList;
    }

    /**
     * 错误数据导出
     * @throws IOException
     */
    private void exportErrorExcel() throws IOException {
        //错误结果集
        List<T> userResultList = errList.stream().map(excelImportErrObjectDto -> {
            T t = null;
            try {
                ObjectMapper om = new ObjectMapper();
                t = om.readValue(om.writeValueAsString(excelImportErrObjectDto.getObject()), clazz);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return t;
        }).collect(Collectors.toList());
        List<Map<Integer,String>> errMsgList = errList.stream().map(excelImportErrObjectDto -> {
            return excelImportErrObjectDto.getCellMap();
        }).collect(Collectors.toList());
        if (userResultList.size()>0){
            //导出excel
            EasyExcelUtils.webWriteExcel(response,userResultList,clazz,errMsgList,"导入错误信息");
        }
    }
}