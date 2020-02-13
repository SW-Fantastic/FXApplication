package org.swdc.fx;

import org.swdc.fx.extra.ExtraManager;
import org.swdc.fx.extra.ExtraModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 应用主容器，维持着一个FXApplication引用
 * 以及作为其他子容器的容器。
 */
public class ApplicationContainer extends Container<Container> {

    private HashMap<Class<? extends Container>, Container> containers = new HashMap<>();

    private FXApplication application;

    public ApplicationContainer(FXApplication application) {
        this.application = application;
    }

    public FXApplication getApplication() {
        return application;
    }

    @Override
    public <R extends Container> R getComponent(Class<R> clazz) {
        if (containers.containsKey(clazz)) {
            return (R)containers.get(clazz);
        } else {
            return (R)register(clazz);
        }
    }

    @Override
    public <R extends Container> Container register(Class<R> clazz) {
        if (containers.containsKey(clazz)) {
            return containers.get(clazz);
        }
        try {
            Container container = clazz.getConstructor().newInstance();
            containers.put(clazz,container);
            container.setScope(this);

            if (clazz != ExtraManager.class) {
                ExtraManager extraManager = getComponent(ExtraManager.class);
                List<ExtraModule> extras = extraManager.listComponents();
                for (ExtraModule module: extras) {
                    if (module.support(clazz)) {
                        container.registerExtra(module);
                    }
                }
            }

            container.initialize();

            return container;
        } catch (Exception ex) {
            logger.error("fail to construct container: " + clazz);
            return null;
        }
    }

    @Override
    public List<Container> listComponents() {
        return new ArrayList<>(containers.values());
    }

}
