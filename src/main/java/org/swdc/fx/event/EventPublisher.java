package org.swdc.fx.event;

import org.swdc.fx.AppComponent;
import org.swdc.fx.anno.Listener;
import org.swdc.fx.container.ComponentScope;
import org.swdc.fx.event.EventListener.*;
import org.swdc.fx.anno.Scope;
import org.swdc.fx.anno.ScopeType;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class EventPublisher {

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
        Class clazz = component.getClass();
        Scope scope = (Scope) clazz.getAnnotation(Scope.class);
        ScopeType type = scope == null ? ScopeType.SINGLE : scope.value();
        ComponentScope componentScope = findScope(type);
        if (componentScope == null) {
            return;
        }
        if (componentScope.singleton()) {
            List<EventListener> listeners = this.listeners.get(event);
            if (listeners == null) {
                listeners = new ArrayList<>();
            }
            listeners.add(EventListener.createListener(method,component));
            this.listeners.put(event,listeners);
        } else {
            List<EventListener> listeners = this.listeners.get(event);
            ProxyMultiTargetListener listenerItem = null;
            if (listeners != null) {
                for (EventListener listener : listeners) {
                    if (listener instanceof EventListener.ProxyMultiTargetListener) {
                        ProxyMultiTargetListener item = (ProxyMultiTargetListener) listener;
                        if (listenerItem.getTargetClazz() == clazz) {
                            listenerItem = item;
                        }
                        break;
                    }
                }
            }
            if (listeners == null) {
                listeners = new ArrayList<>();
                this.listeners.put(clazz,listeners);
            }
            listeners.add(listenerItem);
            listenerItem.addTarget(component);
        }
    }

    public void removeListener(AppComponent component) {
        Class clazz = component.getClass();
        Scope scope = (Scope)clazz.getAnnotation(Scope.class);
        ScopeType type = scope == null ? ScopeType.SINGLE : scope.value();
        ComponentScope componentScope = findScope(type);
        if (componentScope == null) {
            return;
        }
        if (componentScope.singleton()) {
            this.listeners.remove(component.getClass());
        } else {

            ProxyMultiTargetListener listenerItem = null;
            List<EventListener> listeners = this.listeners.get(component.getClass());

            if (listeners == null) {
                return;
            }

            for (EventListener listener : listeners) {
                if (listener instanceof EventListener.ProxyMultiTargetListener) {
                    ProxyMultiTargetListener item = (ProxyMultiTargetListener) listener;
                    if (listenerItem.getTargetClazz() == clazz) {
                        listenerItem = item;
                    }
                    break;
                }
            }
            if (listenerItem == null) {
                return;
            }
            listenerItem.removeTarget(component);
        }

    }

    protected abstract ComponentScope findScope(ScopeType type);

}
