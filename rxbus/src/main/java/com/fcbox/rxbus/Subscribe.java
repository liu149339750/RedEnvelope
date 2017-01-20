package com.fcbox.rxbus;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 订阅回调方法方式注解 .
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Subscribe {

    /**
     * 处理事件线程模式
     */
    ThreadMode threadMode() default ThreadMode.POSTING;

    /**
     * 是否有粘性，即是否接收之前旧的相同的消息类型事件 .
     */
    boolean sticky() default false;

    /**
     * 优先级
     */
    int priority() default 0;
}
