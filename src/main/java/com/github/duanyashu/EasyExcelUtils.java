package com.github.duanyashu;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.github.duanyashu.listener.EasyExcelListener;
import org.apache.poi.ss.usermodel.IndexedColors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: duanyashu
 * @time: 2020-07-10 09:47
 */
public class EasyExcelUtils {

    /**
     * 导出excel方法
     * @param response
     * @param objects 导出的数据
     * @param clazz   导出的对象类型
     * @param fileName  文件名
     * @throws IOException
     */
    public static void webWriteExcel(HttpServletResponse response, List objects, Class clazz , String fileName) throws IOException {
        webWriteExcel(response,objects,clazz,null,fileName);
    }
    public static void webWriteExcel(HttpServletResponse response, List objects, Class clazz,List<Map<Integer,String>> errMsgList , String fileName) throws IOException {
        String sheetName = fileName;
        webWriteExcel(response,objects,clazz,errMsgList,fileName,sheetName);
    }

    public static void webWriteExcel(HttpServletResponse response, List objects, Class clazz, List<Map<Integer,String>> errMsgList , String fileName, String sheetName) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName + "." + ExcelTypeEnum.XLSX.getValue(), "UTF-8"));
        //标题样式
        WriteCellStyle headCellStyle = new WriteCellStyle();
        headCellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        //数据样式
        WriteCellStyle contentCellStyle = new WriteCellStyle();
        HorizontalCellStyleStrategy horizontalCellStyleStrategy =
                new HorizontalCellStyleStrategy(headCellStyle, contentCellStyle);
        ServletOutputStream outputStream = response.getOutputStream();
        try {
            ExcelWriterBuilder write = EasyExcel.write(outputStream, clazz);
            if (errMsgList!=null){
                //inMemory(Boolean.TRUE)开启批注   批注在ErrorSheetWriteHandler中实现
                write.inMemory(Boolean.TRUE)
                        .registerWriteHandler(new ErrorSheetWriteHandler(errMsgList));
            }
            write.registerWriteHandler(horizontalCellStyleStrategy).sheet(sheetName).doWrite(objects);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            outputStream.close();
        }
    }

    /**
     * excel导入方法
     * @param fileInputStream 导入文件
     * @param clazz 对应的实体类
     * @return 处理监听器
     * @throws Exception
     */
    public static EasyExcelListener webImportExcel(HttpServletResponse response, InputStream fileInputStream, Class clazz) throws Exception{
        return webImportExcel(response,fileInputStream,null,clazz,true);
    }

    /**
     * excel导入方法
     * @param fileInputStream 导入文件
     * @param clazz 对应的实体类
     * @param isErrorExport   是否导出错误数据 默认true
     * @return 处理监听器
     * @throws Exception
     */
    public static EasyExcelListener webImportExcel(HttpServletResponse response,InputStream fileInputStream, Class clazz, boolean isErrorExport) throws Exception{
        return webImportExcel(response,fileInputStream,null,clazz,isErrorExport);
    }

    /**
     * excel导入方法
     * @param fileInputStream 导入文件
     * @param customCheckService  自定义校验服务 service接口继承 ExcelCheckManager实现自主校验
     * @param clazz 对应的实体类
     * @return 处理监听器
     * @throws Exception
     */
    public static EasyExcelListener webImportExcel(HttpServletResponse response,InputStream fileInputStream,ExcelCheckManager customCheckService,Class clazz) throws Exception{
        return webImportExcel(response,fileInputStream,customCheckService,clazz,true);
    }

    /**
     *
     * excel导入方法
     * @param fileInputStream  导入文件
     * @param customCheckService  自定义校验服务 service接口继承 ExcelCheckManager实现自主校验
     * @param clazz  对应的实体类
     * @param isErrorExport   是否导出错误数据 默认true
     * @return  处理监听器 可以获取错误数据
     * @throws Exception
     */
    public static EasyExcelListener webImportExcel(HttpServletResponse response, InputStream fileInputStream, ExcelCheckManager customCheckService, Class clazz, boolean isErrorExport) throws Exception{
        EasyExcelListener easyExcelListener = new EasyExcelListener(response,customCheckService, clazz);
        EasyExcel.read(fileInputStream,clazz,easyExcelListener).sheet().doRead();
        return  easyExcelListener;
    }


}
