package org.swdc.fx.anno;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Scope {

    /**
     * 组件的创建模式。
     * @return 创建模式
     */
    ScopeType value() default ScopeType.SINGLE;

}
