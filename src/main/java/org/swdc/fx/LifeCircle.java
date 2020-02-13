package org.swdc.fx;

/**
 * 生命周期
 * container初始化一个组件后，
 * 应该调用他的initialize方法，
 * 在destroy的时候，会调用他的destroy方法。
 */
public interface LifeCircle {

    default void initialize() {

    }

    default void destroy() {

    }

}
