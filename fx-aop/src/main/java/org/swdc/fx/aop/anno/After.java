package org.swdc.fx.aop.anno;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface After {

    String pattern();

    Class annotationWith() default Annotation.class;

}
