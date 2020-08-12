package org.swdc.fx.aop.anno;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Before {

    String pattern();

    Class annotationWith() default Annotation.class;

}
