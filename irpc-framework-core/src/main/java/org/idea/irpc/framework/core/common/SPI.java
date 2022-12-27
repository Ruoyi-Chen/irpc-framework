package org.idea.irpc.framework.core.common;

import java.lang.annotation.*;

/**
 * @Author : Ruoyi Chen
 * @create 2022/12/27 22:52
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SPI {
    String value() default "";
}
