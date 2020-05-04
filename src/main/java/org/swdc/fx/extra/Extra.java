package org.swdc.fx.extra;

import org.swdc.fx.container.ApplicationContainer;
import org.swdc.fx.container.Container;

public interface Extra<R> {

    /**
     * 提供环境，供Extra容器初始化
     * @param container
     * @return
     */
    boolean initialize(ApplicationContainer container);

    /**
     * 销毁Extra模块
     * @param container
     * @return
     */
    boolean destroy(ApplicationContainer container);

    /**
     * 检查该Extra是否支持此容器
     * 如果支持那么在容器中的组件就会受到影响
     * 并且容器组件在register后应该调用activeExtra
     * 以让拓展生效
     *
     * @param container
     * @param <T>
     * @return
     */
    <T extends Container> boolean support(Class<T> container);

    /**
     * 处理某一类型的组件，在组件被初始化后
     * 回调
     * @param comp
     */
    Object postProcess(Object comp);

    /**
     * 清除Extra的影响，在destroy的时候回调
     * @param comp
     */
    void disposeOnComponent(R comp);

}
