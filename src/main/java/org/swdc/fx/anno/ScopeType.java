package org.swdc.fx.anno;

import org.swdc.fx.container.CacheScope;
import org.swdc.fx.container.ComponentScope;
import org.swdc.fx.container.SingletonScope;

/**
 * 组件的创建模式
 */
public enum ScopeType {
    /**
     * 单例，容器会将此类组件放入map缓存。
     */
    SINGLE(SingletonScope.class),

    /**
     * 多实例，初始化后脱离容器，不受管理。
     */
    MULTI(null),

    /**
     * 多实例，但是每一个实例都受控。
     * 必须使用findExisted才能得到已创建的这种对象。
     */
    CACHE(CacheScope.class);

    Class scopeType;

    ScopeType(Class<? extends ComponentScope> clazz) {
        this.scopeType = clazz;
    }

    public Class getScopeType() {
        return scopeType;
    }
}
