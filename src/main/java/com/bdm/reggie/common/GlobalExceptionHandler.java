package com.bdm.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileNotFoundException;
import java.sql.SQLIntegrityConstraintViolationException;


/**
 * @code Description 全局异常处理
 * @code author 本当迷
 * @code date 2022/8/4-8:36
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 用户名重复异常处理方法
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String>exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.info(ex.getMessage());

        if(ex.getMessage().contains("Duplicate entry")){
            final String[] split = ex.getMessage().split(" ");
            final String msg = split[2] + "已存在";
            return R.error(msg);
        }

        return R.error("未知错误！");
    }

    @ExceptionHandler(CustomException.class)
    public R<String>exceptionHandler(CustomException ex){
        log.info(ex.getMessage());

        return R.error(ex.getMessage());
    }

    /**
     * 找不到文件异常
     * @param ex
     * @return
     */
    @ExceptionHandler(FileNotFoundException.class)
    public R<String>exceptionHandler(FileNotFoundException ex){
        log.info(ex.getMessage());

        return R.error(ex.getMessage());
    }

}
