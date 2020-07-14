package com.github.duanyashu;

import com.alibaba.excel.annotation.ExcelProperty;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.groups.Default;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @description: 注解正则校验方法
 * @author: duanyashu
 * @time: 2020-07-10 09:20
 */
public class EasyExcelValiHelper {

    private static Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    public static <T> Map<Integer,String> validateEntity(T obj) throws NoSuchFieldException, SecurityException {
        Map<Integer,String> resultMap = new HashMap<>();
        Set<ConstraintViolation<T>> set = validator.validate(obj, Default.class);
        if (set != null && set.size() != 0) {
            int i =0;
            for (ConstraintViolation<T> cv : set) {
                Field declaredField = obj.getClass().getDeclaredField(cv.getPropertyPath().toString());
                ExcelProperty annotation = declaredField.getAnnotation(ExcelProperty.class);
                //拼接错误信息，包含当前出错数据的标题名字+错误信息
                int index = annotation.index();
                if (index==-1){
                    index = i;
                }
                resultMap.put(index,cv.getMessage());
                i++;
            }
        }

        return resultMap.size()==0? null:resultMap;
    }
}
