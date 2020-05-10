package org.swdc.fx.container;

import org.swdc.fx.anno.ScopeType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public interface ComponentScope<T> {

    Object get(Class clazz);

    List listAll();

    List<Class> getAllClasses();

    default List list(Predicate condition){
        List arrayList = new ArrayList<>();
        for (Object item : listAll()) {
            if (condition.test(item)) {
                arrayList.add(item);
            }
        }
        return arrayList;
    }

    void put(Class clazz, Object target);

    boolean singleton();

    boolean eventListenable();

    ScopeType getType();

}
