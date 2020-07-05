package com.bhy.translatefree.config.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @description: LOG AOP
 * @author: binhy
 * @create: 2019/11/27
 **/
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {

    /**
     * log 说明
     *
     * @return
     */
    String value() default "AOP Logger:";

    /**
     * 是否打印 默认true，不想打印就不使用本注解就行
     *
     * @return
     */
    boolean needLog() default true;
}
