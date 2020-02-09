package org.swdc.fx.services;

import org.swdc.fx.ApplicationContainer;
import org.swdc.fx.Container;
import org.swdc.fx.anno.Scope;
import org.swdc.fx.anno.ScopeType;

import java.lang.reflect.Constructor;
import java.util.HashMap;

public class ServiceManager extends Container<Object> {

    private HashMap<Class, Service> services = new HashMap<>();

    @Override
    public <R> R getComponent(Class<R> clazz) {
        if (services.containsKey(clazz)) {
            return (R)services.get(clazz);
        } else {
            return (R)register(clazz);
        }
    }

    @Override
    public <R> Object register(Class<R> clazz) {
        if (services.containsKey(clazz)) {
            return (R) services.get(clazz);
        } else {
            try {
                Scope scope = clazz.getAnnotation(Scope.class);
                Constructor constructorWithoutArgs = clazz.getConstructor();
                Service service = (Service) constructorWithoutArgs.newInstance();
                service.setContainer((ApplicationContainer) getScope());
                if (scope == null || scope.value() == ScopeType.SINGLE) {
                    services.put(clazz,service);
                    logger.info(" service : " + clazz.getSimpleName() + " loaded");
                }
                return service;
            } catch (Exception ex) {
                logger.error("can not load service " + clazz.getSimpleName());
                return null;
            }
        }
    }

}
