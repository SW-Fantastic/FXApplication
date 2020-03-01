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

    @Override
    protected <R extends ExtraModule> R instance(Class<R> clazz) {
        try {
            ApplicationContainer container = (ApplicationContainer)this.getScope();

            ExtraModule module = clazz.getConstructor().newInstance();
            module.setScope(this);
            module.initialize(container);
            module.initialize();
            for (Object containerItem: container.listComponents()) {
                Container subContainer = (Container)containerItem;
                if (module.support(containerItem.getClass())) {
                    subContainer.registerExtra(module);
                }
            }
            logger.info("extra module loaded:" + module.getClass().getSimpleName());
            return (R)module;
        } catch (Exception ex) {
            logger.error("fail to load extra module : " + clazz.getSimpleName(),ex);
            return null;
        }
    }

    public <R extends ExtraModule> void unRegister(Class<R> clazz) {
        ExtraModule extraModule = this.getComponent(clazz);
        ApplicationContainer container = (ApplicationContainer)this.getScope();
        for (Object containerItem : container.listComponents()) {
            if (extraModule.support(containerItem.getClass())) {
                Container subContainer = (Container)containerItem;
                subContainer.unRegisterExtra(extraModule);
            }
        }
        extraModule.destroy(container);
        extraModule.destroy();
    }

    @Override
    public boolean isComponentOf(Class clazz) {
        return ExtraModule.class.isAssignableFrom(clazz);
    }

    @Override
    public void destroy() {
        for (Class extraModule : this.getRegisteredClass()) {
            this.unRegister(extraModule);
        }
    }
}
