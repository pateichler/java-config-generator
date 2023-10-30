package com.pat_eichler.config.processor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface ProcessConfig {
    String defaultsFileName() default "";
    String infoFileName() default "";
}
