package com.kiin.springmvc.annotation;

import java.lang.annotation.*;

/**
 * @author kiin
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {
    String value() default "";
}
