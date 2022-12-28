package org.idea.irpc.framework.spring.starter.common;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/28 16:55
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface IRpcService {
    int limit() default 0;

    String group() default "default";

    String serviceToken() default "";
}
