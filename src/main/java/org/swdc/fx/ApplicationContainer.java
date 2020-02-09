package org.swdc.fx;

import java.util.HashMap;

public class ApplicationContainer extends Container<Container> {

    private HashMap<Class<? extends Container>, Container> containers = new HashMap<>();

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
            return container;
        } catch (Exception ex) {
            logger.error("fail to construct container: " + clazz);
            return null;
        }
    }

}
