package org.swdc.fx.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.AppComponent;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@FunctionalInterface
public interface EventListener<T> {

    void handler(AppEvent<T> event);

    static ProxyEventListener createListener(Method method, AppComponent target) {
        return new ProxyEventListener(method,target);
    }

    static ProxyMultiTargetListener createListener(Class clazz,Method method) {
        return new ProxyMultiTargetListener(method,clazz, new ArrayList<>());
    }

    class ProxyEventListener<T> implements EventListener<T>  {

        private Method method;
        private AppComponent target;
        private static final Logger logger = LoggerFactory.getLogger(EventListener.class);

        ProxyEventListener(Method method, AppComponent target) {
            this.method = method;
            this.target = target;
        }

        @Override
        public void handler(AppEvent<T> event) {
            try {
                method.invoke(target, event);
            } catch (Exception ex) {
                logger.error("fail to execute event handler : " + target.getClass().getSimpleName() + " " + method.getName(), ex);
            }
        }
    }

    class ProxyMultiTargetListener<T> implements EventListener<T> {

        private Method method;
        private List<AppComponent> target;
        private Class targetClass;
        private static final Logger logger = LoggerFactory.getLogger(EventListener.class);

        ProxyMultiTargetListener(Method method,Class targetClazz, List<AppComponent> target) {
            this.method = method;
            this.target = target;
            this.targetClass = targetClazz;
        }

        @Override
        public void handler(AppEvent<T> event) {
            try {
                method.invoke(target, event);
            } catch (Exception ex) {
                logger.error("fail to execute event handler : " + target.getClass().getSimpleName() + " " + method.getName(), ex);
            }
        }

        public void addTarget(AppComponent component) {
            target.add(component);
        }

        public void removeTarget(AppComponent component) {
            target.remove(component);
        }

        public Class getTargetClazz() {
            return targetClass;
        }

    }

}
