package org.swdc.fx.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Scope {

    /**
     * 组件的创建模式。
     * @return 创建模式
     */
    ScopeType value() default ScopeType.SINGLE;

}
