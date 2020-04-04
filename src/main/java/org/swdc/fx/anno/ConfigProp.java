package org.swdc.fx.anno;

import org.swdc.fx.properties.AbstractPropEditor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by lenovo on 2019/6/8.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigProp {

    /**
     * 配置类型，用于生成编辑器的
     * @return
     */
    PropType type();

    /**
     * 配置名称，出现在本属性的
     * 编辑器左侧
     * @return
     */
    String name();

    /**
     * 属性值，某些编辑器需要属性值
     * 进行初始化
     * @return
     */
    String value();

    /**
     * 提示文本
     * 鼠标悬停在属性名的时候显示
     * @return
     */
    String tooltip();

    /**
     * 属性名，写入properties的时候
     * 使用prefix和本属性结合存储
     * @return
     */
    String propName();

    /**
     * 属性处理器，
     * 用于处理属性涉及的资源
     * 例如主题的导入就需要这个。
     * @return
     */
    Class<? extends PropResolver> resolver() default PropResolver.class;

    /**
     * 指定编辑器的类，需要在Type为Custom的时候使用
     * @return
     */
    Class<? extends AbstractPropEditor> editor() default AbstractPropEditor.class;
}
