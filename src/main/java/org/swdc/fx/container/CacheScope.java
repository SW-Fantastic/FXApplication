package org.swdc.fx.container;

import org.swdc.fx.anno.ScopeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CacheScope<T> implements ComponentScope<T> {

    private Map<Class, List<Object>> cache = new HashMap<>();

    @Override
    public Object getDefault(Class clazz) {
        return cache.containsKey(clazz) ? cache.get(clazz).get(0):null;
    }

    @Override
    public List listAll() {
        return cache.entrySet().stream()
                .flatMap(ent->ent.getValue().stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<Class> getAllClasses() {
        return new ArrayList<>(cache.keySet());
    }

    @Override
    public void put(Class clazz, Object target) {
        List<Object> components = cache.get(clazz);
        if (components == null) {
            components = new ArrayList<>();
            cache.put(clazz, components);
        }
        if (!components.contains(target)) {
            components.add(target);
        }
    }

    @Override
    public boolean singleton() {
        return false;
    }

    @Override
    public boolean eventListenable() {
        return true;
    }

    @Override
    public ScopeType getType() {
        return ScopeType.CACHE;
    }
}
