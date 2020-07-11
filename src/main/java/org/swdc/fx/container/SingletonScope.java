package org.swdc.fx.container;

import org.swdc.fx.AppComponent;
import org.swdc.fx.anno.ScopeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SingletonScope<T extends AppComponent> implements ComponentScope<T > {

    private HashMap<Class, Object> components = new HashMap<>();

    @Override
    public List listAll() {
        return new ArrayList(components.values());
    }

    @Override
    public List<Class> getAllClasses() {
        return new ArrayList<>(components.keySet());
    }

    @Override
    public void put(Class clazz, Object target) {
        if (components.containsKey(clazz)) {
            return;
        }
        if (!Container.class.isAssignableFrom(clazz)) {
            for (Class clazzItem : components.keySet()) {
                if (clazzItem.isAssignableFrom(clazz)) {
                    return;
                }
            }
        }
        components.put(clazz,target);
    }

    @Override
    public Object get(Class clazz) {
        if (!components.containsKey(clazz)) {
            return null;
        }
        return components.get(clazz);
    }

    @Override
    public boolean singleton() {
        return true;
    }

    @Override
    public boolean eventListenable() {
        return true;
    }

    @Override
    public ScopeType getType() {
        return ScopeType.SINGLE;
    }
}
