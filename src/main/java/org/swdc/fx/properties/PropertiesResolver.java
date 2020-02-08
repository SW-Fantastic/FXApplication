package org.swdc.fx.properties;

public interface PropertiesResolver<T extends FXProperties> {

     void saveProperties(T props);

     T load(Class<T> clazz);

     T refresh(T target);

}
