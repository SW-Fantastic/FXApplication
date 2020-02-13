package org.swdc.fx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.extra.ExtraManager;
import org.swdc.fx.extra.ExtraModule;

import java.util.HashMap;
import java.util.List;

/**
 * 容器，管理各类资源的那种。
 * @param <T>
 */
public abstract class Container<T> implements LifeCircle {

    /**
     * 父容器，一般在创建的时候会指定。
     * ApplicationContainer的父容器为null
     */
    private Container<Container> scope;

    /**
     * 拓展注册表，对本容器可用的拓展模块会在这里出现
     */
    private HashMap<Class, ExtraModule> extraMap = new HashMap<>();

    /**
     * 返回父容器
     * @return
     */
    public Container<Container> getScope() {
        return scope;
    }

    protected void setScope(Container<Container> scope) {
        this.scope = scope;
    }

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 获取组件，没有就创建一个
     * @param clazz 组件类
     * @param <R> 组件泛型
     * @return 组件实例
     */
    public abstract <R extends T> R getComponent(Class<R> clazz);

    /**
     * 注册组件，有就直接返回，没有会创建一个新的。
     * @param clazz 组件类
     * @param <R> 组件泛型
     * @return 组件实例
     */
    public abstract <R extends T> T register(Class<R> clazz);

    /**
     * 列出所有组件
     * @param <R> 组件泛型
     * @return
     */
    public abstract <R extends T> List<R> listComponents();

    /**
     * 注册拓展模块。
     * 如果拓展模块适用于本容器，他会被放入map。
     * @param module 拓展模块
     */
    public void registerExtra(ExtraModule module) {
        if (module == null) {
            return;
        }
        if (this.extraMap.containsKey(module.getClass())) {
            return;
        }
        if (module.support(this.getClass())) {
            this.extraMap.put(module.getClass(),module);
        }
    }

    /**
     * 解注册模块
     * @param module 模块
     */
    public void unRegisterExtra(ExtraModule module) {
        if (extraMap.containsKey(module.getClass())) {
            this.destroyExtras(module);
            extraMap.remove(module.getClass());
        }
    }

    /**
     * 在此组件上激活拓展模块，让拓展模块对组件进行处理。
     * @param target 组件
     */
    protected void activeExtras(T target) {
        for (ExtraModule module: extraMap.values()) {
            module.activeOnComponent(target);
        }
    }

    /**
     * 在此组件上销毁拓展模块，消除拓展模块的影响
     * @param module 模块
     */
    protected void destroyExtras(ExtraModule module) {
        for (T target: this.listComponents()) {
            module.disposeOnComponent(target);
        }
    }

    /**
     * 销毁容器，销毁容器中每一个组件。
     */
    @Override
    public void destroy() {
        if (this.getClass() != ExtraManager.class) {
            for (ExtraModule module : extraMap.values()) {
                this.unRegisterExtra(module);
            }
        }
        List components = this.listComponents();
        if (components == null) {
            return;
        }
        for (Object object: components) {
            if (object instanceof LifeCircle) {
                LifeCircle target = (LifeCircle)object;
                target.destroy();
            }
        }
    }
}
