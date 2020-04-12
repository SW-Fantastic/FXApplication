package org.swdc.fx.net;

import org.swdc.fx.FXApplication;

import java.lang.reflect.ParameterizedType;

public abstract class ExternalHandler<T> {

    public abstract void accept(FXApplication attachment, T message);

    public boolean support(Class target) {
        ParameterizedType clazz = (ParameterizedType)this.getClass().getGenericSuperclass();
        Class type = (Class)clazz.getActualTypeArguments()[0];
        return target == type;
    }

}
