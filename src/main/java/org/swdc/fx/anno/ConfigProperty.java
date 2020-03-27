package org.swdc.fx.anno;


import org.controlsfx.property.BeanProperty;

import java.beans.PropertyDescriptor;

/**
 * 属性配置描述器
 * 描述一个属性在生成编辑器的时候应该怎么
 * 进行处理
 */
public class ConfigProperty extends BeanProperty {

    private ConfigProp propData;

    public ConfigProperty(Object bean, PropertyDescriptor propertyDescriptor, ConfigProp prop) {
        super(bean, propertyDescriptor);
        propData = prop;
    }

    public ConfigProp getPropData() {
        return propData;
    }

    @Override
    public String getName() {
        return propData.name();
    }

    @Override
    public String getDescription() {
        return propData.tooltip();
    }
}
