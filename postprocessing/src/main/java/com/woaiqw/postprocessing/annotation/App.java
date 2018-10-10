package com.woaiqw.postprocessing.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by haoran on 2018/10/10.
 */

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface App {

    String name() default "Main";

    int priority() default 0;

    boolean async() default false;

    long delay() default 0;

}
