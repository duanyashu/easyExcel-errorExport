package cn.tuodayun;

import java.util.ArrayList;
import java.util.List;

/**
 * @description: excel数据导入结果
 * @author: duanyashu
 * @time: 2020-07-10 09:18
 */
public class ExcelCheckResult<T> {

    /**
     * 成功数据结果集
     */
    private List<T> successDtos;

    /**
     * 错误数据结果集
     */
    private List<ExcelImportErrDto> errDtos;

    public ExcelCheckResult(List<T> successDtos, List<ExcelImportErrDto> errDtos){
        this.successDtos =successDtos;
        this.errDtos = errDtos;
    }

    public ExcelCheckResult(List<ExcelImportErrDto> errDtos){
        this.successDtos =new ArrayList<>();
        this.errDtos = errDtos;
    }

    public List<T> getSuccessDtos() {
        return successDtos;
    }

    public void setSuccessDtos(List<T> successDtos) {
        this.successDtos = successDtos;
    }

    public List<ExcelImportErrDto> getErrDtos() {
        return errDtos;
    }

    public void setErrDtos(List<ExcelImportErrDto> errDtos) {
        this.errDtos = errDtos;
    }
}
