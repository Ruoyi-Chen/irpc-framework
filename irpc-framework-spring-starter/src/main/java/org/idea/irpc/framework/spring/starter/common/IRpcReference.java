package org.idea.irpc.framework.spring.starter.common;

import java.lang.annotation.*;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/28 16:54
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IRpcReference {
    String url() default "";
    String group() default "default";
    String serviceToken() default "";
    int timeOut() default 3000;
    int retry() default 1;
    boolean async() default false;
}
