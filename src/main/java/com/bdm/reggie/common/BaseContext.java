package com.bdm.reggie.common;

/**
 * @code Description 基于ThreadLocal封装的工具类，用户保存和获取当前登录用户id
 * @code author 本当迷
 * @code date 2022/8/5-7:39
 */
public class BaseContext {
    private static final ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 设置值
     * @param id
     */
    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    /**
     * 获取值
     * @return
     */
    public static Long getCurrentId(){
        return threadLocal.get();
    }
}