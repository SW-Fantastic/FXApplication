package org.swdc.fx.container;

import org.swdc.fx.FXApplication;
import org.swdc.fx.extra.ExtraManager;
import org.swdc.fx.extra.ExtraModule;

import java.util.List;

/**
 * 应用主容器，维持着一个FXApplication引用
 * 以及作为其他子容器的容器。
 */
public class ApplicationContainer extends Container<Container> {

    private FXApplication application;

    public ApplicationContainer(FXApplication application) {
        this.application = application;
    }

    public FXApplication getApplication() {
        return application;
    }

    @Override
    protected Container instance(Class clazz) {
        try {
            Container container = (Container) clazz.getConstructor().newInstance();
            container.setParent(this);

            if (clazz != ExtraManager.class) {
                ExtraManager extraManager = getComponent(ExtraManager.class);
                List<ExtraModule> extras = extraManager.listComponents();
                for (ExtraModule module: extras) {
                    if (module.support(clazz)) {
                        container.registerExtra(module);
                    }
                }
            }

            return container;
        } catch (Exception ex) {
            logger.error("fail to construct container: " + clazz);
            return null;
        }
    }

    @Override
    public boolean isComponentOf(Class clazz) {
        return Container.class.isAssignableFrom(clazz);
    }
}
