package com.woaiqw.orm.annotation.sqlite;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * @title Id主键配置
 * @description 不配置的时候默认找类的id或_id字段作为主键，column不配置的是默认为字段名
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME) 
public @interface Id {
	 public String column() default "";
}
