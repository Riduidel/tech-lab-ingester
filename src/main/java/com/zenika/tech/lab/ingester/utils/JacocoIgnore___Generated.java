package com.zenika.tech.lab.ingester.utils;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation created to satisfy Jacoco needs.
 * Use it when you want Jacoco to ignore some code.
 * 
 * @see https://www.baeldung.com/jacoco-report-exclude
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface JacocoIgnore___Generated {

}
