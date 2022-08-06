package com.example.common;
/**
 * 基于ThreadLocal封装工具类，用户保存和获取当前登录用户id
 */
public class BaseContext {
    private static ThreadLocal<Long> threadLocal=new ThreadLocal<>();
    /**
     * 设置值(存值)
     */
    public static void setCurrentId(Long id){
          threadLocal.set(id);
    }
    /**
     * 取值
     */
    public static Long getCurrentId(){
        Long aLong = threadLocal.get();
        return aLong;
    }
}
