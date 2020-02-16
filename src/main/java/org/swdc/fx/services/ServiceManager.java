package org.swdc.fx.services;

import org.swdc.fx.AppComponent;
import org.swdc.fx.ApplicationContainer;
import org.swdc.fx.Container;
import org.swdc.fx.FXApplication;
import org.swdc.fx.anno.Scope;
import org.swdc.fx.anno.ScopeType;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ServiceManager extends Container<Service> {

    private HashMap<Class, Service> services = new HashMap<>();

    @Override
    public <R extends Service> R getComponent(Class<R> clazz) {
        if (!isComponentOf(clazz)) {
            return null;
        }
        if (services.containsKey(clazz)) {
            return (R)services.get(clazz);
        } else {
            return (R)register(clazz);
        }
    }

    @Override
    public <R extends Service> R register(Class<R> clazz) {
        if (!isComponentOf(clazz)) {
            return null;
        }
        if (services.containsKey(clazz)) {
            return (R) services.get(clazz);
        } else {
            try {
                Scope scope = clazz.getAnnotation(Scope.class);
                Constructor constructorWithoutArgs = clazz.getConstructor();
                Service service = (Service) constructorWithoutArgs.newInstance();
                service.setContainer((ApplicationContainer) getScope());

                this.activeExtras(service);

                if (clazz.getModule().isOpen(clazz.getPackageName(), FXApplication.class.getModule())) {
                    AppComponent.awareComponents(service);
                }
                service.initialize();

                if (scope == null || scope.value() == ScopeType.SINGLE) {
                    services.put(clazz,service);
                    logger.info(" service : " + clazz.getSimpleName() + " loaded");
                }
                return (R) service;
            } catch (Exception ex) {
                logger.error("can not load service " + clazz.getSimpleName(), ex);
                return null;
            }
        }
    }

    @Override
    public List<Service> listComponents() {
        return new ArrayList<>(services.values());
    }

    @Override
    public boolean isComponentOf(Class clazz) {
        return Service.class.isAssignableFrom(clazz);
    }
}
