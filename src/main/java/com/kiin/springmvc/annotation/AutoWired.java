package com.kiin.springmvc.annotation;

import java.lang.annotation.*;

/**
 * @author kiin
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoWired {
    String value() default "";
}
