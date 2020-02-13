package org.swdc.fx.anno;

import org.swdc.fx.FXSplash;
import org.swdc.fx.FXView;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Application注解，提供必要的信息以启动应用。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SFXApplication {

    /**
     * 需要一个FXSplash类或者是他的子类，
     * 此类需要提供无参数构造函数，Application
     * 启动的时候会使用他作为欢迎界面。
     * @return
     */
    Class<? extends FXSplash> splash();

    /**
     * MainView，初始化完毕后会打开的第一个窗口。
     * 需要继承自FXView的子类。
     * @return
     */
    Class<? extends FXView> mainView();

    /**
     * 资源路径，默认是同级目录外的assets目录，
     * 此目录不包含在classpath中，用来存放资源文件和部分配置
     * @return
     */
    String assetsPath() default "./assets";

}
