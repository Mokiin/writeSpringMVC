package com.kiin.springmvc.annotation;

import java.lang.annotation.*;

/**
 * @author kiin
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {
    String value() default "";
}
