package com.github.duanyashu;

import com.alibaba.excel.write.handler.AbstractRowWriteHandler;
import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.helpers.XSSFRowShifter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description: 错误数据格式处理器
 * @author: duanyashu
 * @time: 2020-07-10 16:05
 */
public class ErrorSheetWriteHandler  extends AbstractRowWriteHandler {

    /**
     * 校验错误文件
     */
    private List<Map<Integer,String>> errMsgList;


    public ErrorSheetWriteHandler(List<Map<Integer,String>> errMsgList){
            this.errMsgList = errMsgList;
    }
    @Override
    public void afterRowDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row,
                                Integer relativeRowIndex, Boolean isHead) {
        //如果false 就读取行数据.
        if (!isHead) {
            Sheet sheet = writeSheetHolder.getSheet();
            //循环是设置批量批示的
            Map<Integer, String> rowErrMap = errMsgList.get(relativeRowIndex);
            for (Map.Entry<Integer, String> cellMap : rowErrMap.entrySet()) {
                    setPostil(sheet, relativeRowIndex, cellMap.getKey(),cellMap.getValue());

            }
            }
    }

    /**
     * 设置样式添加批注信息
     * @param sheet
     * @param relativeRowIndex
     * @param i
     * @param msg
     */
    private void setPostil(Sheet sheet, Integer relativeRowIndex, Integer i,String msg) {
        Workbook workbook = sheet.getWorkbook();
        CellStyle cellStyle = workbook.createCellStyle();
        //设置前景填充样式
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        //设置前景色为红色
        cellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        //设置垂直居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        Drawing<?> drawingPatriarch = sheet.createDrawingPatriarch();
        //创建一个批注
        Comment comment =
                drawingPatriarch.createCellComment(new XSSFClientAnchor(0, 0, 0, 0, 0,0,2,2));
        // 输入批注信息
        comment.setString(new XSSFRichTextString(msg));
        // 将批注添加到单元格对象中
        sheet.getRow(relativeRowIndex + 1).getCell(i).setCellComment(comment);
        sheet.getRow(relativeRowIndex + 1).getCell(i).setCellStyle(cellStyle);
    }

}
