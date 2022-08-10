package com.bdm.reggie.common;

/**
 * @code Description 自定义业务异常
 * @code author 本当迷
 * @code date 2022/8/5-11:00
 */
public class CustomException extends RuntimeException{
    public CustomException(String message){
        super(message);
    }
}
