package org.swdc.fx.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.AppComponent;

import java.lang.reflect.Method;

@FunctionalInterface
public interface EventListener<T> {

    void handler(AppEvent<T> event);

    static ProxyEventListener createListener(Method method, AppComponent target) {
        return new ProxyEventListener(method,target);
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

}
