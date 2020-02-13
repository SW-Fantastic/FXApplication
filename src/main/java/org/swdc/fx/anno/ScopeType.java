package org.swdc.fx.anno;

/**
 * 组件的创建模式
 */
public enum ScopeType {
    /**
     * 单例，容器会将此类组件放入map缓存。
     */
    SINGLE,

    /**
     * 多实例，初始化后脱离容器，不受管理。
     */
    MULTI
}
