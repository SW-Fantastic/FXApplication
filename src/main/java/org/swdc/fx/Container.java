package org.swdc.fx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.anno.Aware;
import org.swdc.fx.anno.Scope;
import org.swdc.fx.anno.ScopeType;
import org.swdc.fx.event.AppEvent;
import org.swdc.fx.event.EventPublisher;
import org.swdc.fx.extra.ExtraManager;
import org.swdc.fx.extra.ExtraModule;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 容器，管理各类资源的那种。
 * @param <T>
 */
public abstract class Container<T> extends EventPublisher implements LifeCircle{

    /**
     * 父容器，一般在创建的时候会指定。
     * ApplicationContainer的父容器为null
     */
    private Container<Container> scope;

    /**
     * 拓展注册表，对本容器可用的拓展模块会在这里出现
     */
    private HashMap<Class, ExtraModule> extraMap = new HashMap<>();

    private HashMap<Class, Object> components = new HashMap<>();

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
    public <R extends T> R getComponent(Class<R> clazz){
        if (!isComponentOf(clazz)) {
            return null;
        }
        if (components.containsKey(clazz)) {
            return (R)components.get(clazz);
        }
        return (R)register(clazz);
    }

    /**
     * 注册组件，有就直接返回，没有会创建一个新的。
     * @param clazz 组件类
     * @param <R> 组件泛型
     * @return 组件实例
     */
    public <R extends T> R register(Class<R> clazz) {
        if (!isComponentOf(clazz)) {
            return null;
        }
        if (components.containsKey(clazz)) {
            return (R)components.get(clazz);
        }
        for (Class item : components.keySet()) {
            if (clazz.isAssignableFrom(item)){
                return (R)components.get(item);
            }
        }
        Object target = instance(clazz);

        if (!(this instanceof ExtraModule) && !(this instanceof ApplicationContainer)) {
            if (clazz.getModule().isOpen(clazz.getPackageName(), FXApplication.class.getModule())) {
                if (target instanceof AppComponent) {
                    AppComponent appComponent = AppComponent.class.cast(target);
                    appComponent.setContainer((ApplicationContainer)this.getScope());
                    this.awareComponents((AppComponent) target);
                    Scope scope = clazz.getAnnotation(Scope.class);
                    // 只有单例对象可以监听
                    if (scope == null || scope.value() == ScopeType.SINGLE) {
                        registerEventHandler(appComponent);
                    }
                }
            }
            if (target instanceof AppComponent) {
                target = this.activeExtras(target);
            }
        }

        if (target instanceof LifeCircle) {
            LifeCircle.class.cast(target).initialize();
        }

        Scope scope = clazz.getAnnotation(Scope.class);
        if (scope != null && scope.value() != ScopeType.SINGLE) {
            return (R)target;
        }
        components.put(clazz, target);
        return (R)target;
    }

    protected List<Class> getRegisteredClass() {
        return new ArrayList<>(components.keySet());
    }

    @Override
    public void registerEventHandler(AppComponent component) {
        Container container = getScope();
        if (container == null){
            super.registerEventHandler(component);
            return;
        }
        while (container.getScope() != null){
            container = container.getScope();
        }
        container.registerEventHandler(component);
    }

    @Override
    public <T extends AppEvent> void emit(T event) {
        Container container = getScope();
        if(container == null) {
            super.emit(event);
            return;
        }
        while (container.getScope() != null){
            container = container.getScope();
        }
        container.emit(event);
    }

    /**
     * 初始化此组件，创建实例对象
     * @param target
     * @param <R>
     * @return
     */
    protected abstract <R extends T> R instance(Class<R> target);

    /**
     * 列出所有组件
     * @return
     */
    public List listComponents() {
        return components.values().stream().collect(Collectors.toList());
    }

    public abstract boolean isComponentOf(Class clazz);

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
    protected Object activeExtras(Object target) {
        for (ExtraModule module: extraMap.values()) {
            target = module.postProcess(target);
        }
        return target;
    }

    /**
     * 在此组件上销毁拓展模块，消除拓展模块的影响
     * @param module 模块
     */
    protected void destroyExtras(ExtraModule module) {
        for (Object target: this.listComponents()) {
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

    public void awareComponents(AppComponent component) {
        try {
            Class clazz = component.getClass();
            while (clazz != null) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    Aware aware = field.getAnnotation(Aware.class);
                    if (aware == null) {
                        continue;
                    }
                    Object target = component.findComponent(field.getType());
                    field.setAccessible(true);
                    field.set(component,target);
                    field.setAccessible(false);
                }
                clazz = clazz.getSuperclass();
                if (clazz == AppComponent.class || clazz == Object.class) {
                    break;
                }
            }
        } catch (Exception ex) {
            logger.error("failed to inject component : " + component.getClass(),ex);
        }
    }
}
