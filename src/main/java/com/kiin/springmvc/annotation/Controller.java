package com.kiin.springmvc.annotation;

import java.lang.annotation.*;

/**
 * @author kiin
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Controller {
    String value() default "";
}
