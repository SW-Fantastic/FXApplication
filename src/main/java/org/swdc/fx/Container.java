package org.swdc.fx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public abstract class Container<T> {

    private Container<Container> scope;

    public Container<Container> getScope() {
        return scope;
    }

    protected void setScope(Container<Container> scope) {
        this.scope = scope;
    }

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private HashMap<Class, T> managedMap = new HashMap<>();

    public abstract <R extends T> R getComponent(Class<R> clazz);

    public abstract <R extends T> T register(Class<R> clazz);

}
