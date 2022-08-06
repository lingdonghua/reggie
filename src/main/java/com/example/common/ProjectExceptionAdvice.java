package com.example.common;

import com.example.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 */
@RestControllerAdvice
@Slf4j
public class ProjectExceptionAdvice {
    /**
     * 数据库重复插入异常处理
     * @param e
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> sqlException(SQLIntegrityConstraintViolationException e){
         log.error(e.getMessage());
         //如果异常中包含Duplicate entry（姓名重复录入）
         if(e.getMessage().contains("Duplicate entry")){
             //按空格切割字符串
             String[] split=e.getMessage().split(" ");
             String msc=split[2]+"已存在";
             return R.error(msc);
         }
        return R.error("未知错误");
    }

    @ExceptionHandler(BusinessException.class)
    public R<String> BusinessException(BusinessException e){
        log.error(e.getMessage());
        return R.error(e.getMessage());
    }
}
