package org.swdc.fx.services;

import org.swdc.fx.Container;
import java.lang.reflect.Constructor;

public class ServiceManager extends Container<Service> {

    @Override
    protected <R extends Service> R instance(Class<R> clazz) {
        try {
            Constructor constructorWithoutArgs = clazz.getConstructor();
            Service service = (Service) constructorWithoutArgs.newInstance();
            return (R)service;
        } catch (Exception ex) {
            logger.error("fail to init service :" + clazz.getSimpleName(), ex);
            return null;
        }
    }

    @Override
    public boolean isComponentOf(Class clazz) {
        return Service.class.isAssignableFrom(clazz);
    }
}
