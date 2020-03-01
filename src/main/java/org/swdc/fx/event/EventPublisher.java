package org.swdc.fx.event;

import org.swdc.fx.AppComponent;
import org.swdc.fx.anno.Listener;
import org.swdc.fx.anno.Scope;
import org.swdc.fx.anno.ScopeType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EventPublisher {

    private HashMap<Class, List<EventListener>> listeners = new HashMap<>();

    public <T extends AppEvent> void emit(T event) {
        if (listeners.containsKey(event.getClass())) {
            List<EventListener> handlers = listeners.get(event.getClass());
            for (EventListener listener: handlers) {
                listener.handler(event);
            }
        }
    }

    public void registerEventHandler(AppComponent component) {
        Class clazz = component.getClass();
        Scope scope = (Scope) clazz.getAnnotation(Scope.class);
        if (scope != null && scope.value() != ScopeType.SINGLE) {
            return;
        }
        Class parent = clazz;
        while (parent != null) {
            Method[] methods = parent.getDeclaredMethods();
            for (Method method : methods) {
                Listener listener = method.getAnnotation(Listener.class);
                if (listener != null) {
                    handlerListeners(listener.value(), method, component);
                }
            }
            parent = parent.getSuperclass();
            if (parent == AppComponent.class || parent == Object.class) {
                break;
            }
        }
    }

    private void handlerListeners(Class event, Method method, AppComponent component) {
        List<EventListener> listeners = this.listeners.get(event);
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(EventListener.createListener(method,component));
        this.listeners.put(event,listeners);
    }

}
