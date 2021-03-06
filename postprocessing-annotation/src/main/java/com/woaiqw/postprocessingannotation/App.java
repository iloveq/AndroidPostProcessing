package com.woaiqw.postprocessingannotation;

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

    boolean RELEASE = false;
    boolean DEBUG = true;

    String name() default "Main";

    boolean type() default RELEASE;

    int priority() default 0;

    boolean async() default false;

    long delay() default 0;

}
