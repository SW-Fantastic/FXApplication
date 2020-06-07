package org.swdc.fx.container;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.AppComponent;
import org.swdc.fx.FXApplication;
import org.swdc.fx.LifeCircle;
import org.swdc.fx.anno.Aware;
import org.swdc.fx.anno.Scope;
import org.swdc.fx.anno.ScopeType;
import org.swdc.fx.event.AppEvent;
import org.swdc.fx.event.EventPublisher;
import org.swdc.fx.extra.ExtraManager;
import org.swdc.fx.extra.ExtraModule;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;

/**
 * 容器，管理各类资源的那种。
 * @param <T>
 */
public abstract class Container<T> extends EventPublisher implements LifeCircle {

    /**
     * 父容器，一般在创建的时候会指定。
     * ApplicationContainer的父容器为null
     */
    private Container<Container> parent;

    /**
     * 拓展注册表，对本容器可用的拓展模块会在这里出现
     */
    private HashMap<Class, ExtraModule> extraMap = new HashMap<>();

    /**
     * Scope列表
     */
    private Set<ComponentScope<T>> scopes = new HashSet<>();

    /**
     * 返回父容器
     * @return
     */
    public Container<Container> getParent() {
        return parent;
    }

    protected void setParent(Container<Container> parent) {
        this.parent = parent;
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
        Scope scope = clazz.getAnnotation(Scope.class);
        ScopeType scopeType = scope == null ? ScopeType.SINGLE :scope.value();
        for (ComponentScope componentScope: scopes) {
            if (scopeType != componentScope.getType()) {
                continue;
            }
            R target = (R)componentScope.get(clazz);
            if (target != null) {
                return target;
            }
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

        Scope scope = clazz.getAnnotation(Scope.class);
        ScopeType type = scope == null ? ScopeType.SINGLE : scope.value();

        ComponentScope compScope = findScope(type);
        if (compScope != null) {
            Object result = compScope.get(clazz);
            if (result != null) {
                return (R)result;
            }
        }

        try {
            boolean scopeAvailable = type.getScopeType() != null;
            if (scopeAvailable && compScope == null) {
                compScope = (ComponentScope) type.getScopeType().getConstructor().newInstance();
                scopes.add(compScope);
            }
            Object target = instance(clazz);
            if (!(this instanceof ExtraModule) && !(this instanceof ApplicationContainer)) {
                if (clazz.getModule().isOpen(clazz.getPackageName(), FXApplication.class.getModule())) {
                    if (target instanceof AppComponent) {
                        AppComponent appComponent = AppComponent.class.cast(target);
                        appComponent.setContainer((ApplicationContainer)this.getParent());
                        this.awareComponents((AppComponent) target);
                        if (scopeAvailable && compScope.eventListenable()) {
                            registerEventHandler(appComponent);
                        }
                    }
                }
            }
            if (target instanceof AppComponent) {
                target = this.activeExtras(target);
            }
            if (target instanceof LifeCircle) {
                LifeCircle.class.cast(target).initialize();
            }
            if (scopeAvailable) {
                compScope.put(clazz, target);
            }
            return (R)target;
        } catch (Exception e) {
            logger.error("fail to create component ", e);
            return null;
        }
    }

    /**
     * 获取已注册的所有classes
     * @return 已注册的class列表
     */
    protected List<Class> getRegisteredClass() {
        List<Class> classList = new ArrayList<>();
        for (ComponentScope scope : scopes) {
            classList.addAll(scope.getAllClasses());
        }
        return classList;
    }

    /**
     * 注册事件的监听。
     * 内部使用，不公开。
     * container继承了eventPublisher。
     * @param component
     */
    @Override
    public void registerEventHandler(AppComponent component) {
        Container container = getParent();
        if (container == null){
            super.registerEventHandler(component);
            return;
        }
        while (container.getParent() != null){
            container = container.getParent();
        }
        container.registerEventHandler(component);
    }

    /**
     * 发布事件，事件会被监听到。
     * @param event 事件
     * @param <T> 事件类型
     */
    @Override
    public <T extends AppEvent> void emit(T event) {
        Container container = getParent();
        if(container == null) {
            super.emit(event);
            return;
        }
        while (container.getParent() != null){
            container = container.getParent();
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
        List<Object> components = new ArrayList<>();
        for (ComponentScope scope: scopes) {
            components.addAll(scope.listAll());
        }
        return components;
    }

    /**
     * 此类是否为本容器管理的类型。
     * @param clazz 类
     * @return 是否为本容器的组件类。
     */
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
     * 按照一定条件在scope查找对象。
     * @param clazz 对象的类
     * @param condition 条件
     * @param <R> 类的泛型
     * @return 找到的对象或null
     */
    public <R extends T> List<R> findAllScopedComponent(Class<R> clazz, Predicate<R> condition) {
        Scope scope = clazz.getAnnotation(Scope.class);
        ScopeType type = scope == null ? ScopeType.SINGLE: scope.value();
        ComponentScope componentScope = findScope(type);
        if (componentScope != null) {
            return componentScope.list(condition);
        }
        return Collections.emptyList();
    }

    /**
     * 获取一个存储对象的scope
     * @param type scope的类型
     * @return scope
     */
    @Override
    public ComponentScope findScope(ScopeType type) {
        for(ComponentScope scope: scopes) {
            if (scope.getType() == type){
                return scope;
            }
        }
        return null;
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
            if (object instanceof AppComponent) {
                removeListener((AppComponent)object);
            }
        }
    }

    /**
     * 装配此组件的依赖。
     * @param component 组件
     */
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
