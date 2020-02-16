package org.swdc.fx.extra;

import org.swdc.fx.ApplicationContainer;
import org.swdc.fx.Container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 拓展模块的容器
 */
public class ExtraManager extends Container<ExtraModule> {

    private HashMap<Class, ExtraModule> extraModules = new HashMap<>();

    @Override
    public <R extends ExtraModule> R getComponent(Class<R> clazz) {
        if (extraModules.containsKey(clazz)) {
            return (R)extraModules.get(clazz);
        } else {
            return (R)register(clazz);
        }
    }

    @Override
    public <R extends ExtraModule> ExtraModule register(Class<R> clazz) {
        try {
            if (extraModules.containsKey(clazz)) {
                return (R)extraModules.get(clazz);
            }

            ApplicationContainer container = (ApplicationContainer)this.getScope();

            ExtraModule module = clazz.getConstructor().newInstance();
            module.setScope(this);
            module.initialize(container);
            module.initialize();
            for (Container containerItem: container.listComponents()) {
                if (module.support(containerItem.getClass())) {
                    containerItem.registerExtra(module);
                }
            }
            logger.info("extra module loaded:" + module.getClass().getSimpleName());
            extraModules.put(clazz, module);
            return module;
        } catch (Exception ex) {
            logger.error("fail to load extra module ", ex);
            return null;
        }
    }

    public <R extends ExtraModule> void unRegister(Class<R> clazz) {
        if (!extraModules.containsKey(clazz)) {
            return;
        }
        ExtraModule extraModule = extraModules.get(clazz);
        ApplicationContainer container = (ApplicationContainer)this.getScope();
        for (Container containerItem : container.listComponents()) {
            if (extraModule.support(containerItem.getClass())) {
                containerItem.unRegisterExtra(extraModule);
            }
        }
        extraModule.destroy(container);
        extraModule.destroy();
    }

    @Override
    public List<ExtraModule> listComponents() {
        return new ArrayList<>(extraModules.values());
    }

    @Override
    public boolean isComponentOf(Class clazz) {
        return ExtraModule.class.isAssignableFrom(clazz);
    }

    @Override
    public void destroy() {
        for (Class extraModule : extraModules.keySet()) {
            this.unRegister(extraModule);
        }
    }
}
