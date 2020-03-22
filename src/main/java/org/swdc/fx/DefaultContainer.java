package org.swdc.fx;

import java.lang.reflect.Constructor;

public abstract class DefaultContainer<T extends AppComponent> extends ScanableContainer<T> {

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
