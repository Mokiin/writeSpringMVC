package com.kiin.springmvc.annotation;

import java.lang.annotation.*;

/**
 * @author kiin
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseBody {
}
