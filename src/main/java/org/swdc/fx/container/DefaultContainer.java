package org.swdc.fx.container;

import org.swdc.fx.AppComponent;

import java.lang.reflect.Constructor;

public abstract class DefaultContainer<T extends AppComponent> extends Container<T> {

    @Override
    public void initialize() {
        this.scanComponentAndInitialize();
    }

    @Override
    protected <R extends T> R instance(Class<R> target) {
        try {
            if (!isComponentOf(target)) {
                return null;
            }
            Constructor constructor = target.getConstructor();
            R instance = (R)constructor.newInstance();
            return instance;
        } catch (NoSuchMethodException exc){
            logger.error("can not found no parameter constructor, if parameter is needed, please override instance method.");
            logger.error("class named :" + target.getName());
        }catch (Exception e) {
            logger.error("fail to instance " + target.getName(), e);
        }
        return null;
    }


}
