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

    private String name;

    private String desc;

    public ConfigProperty(Object bean, PropertyDescriptor propertyDescriptor, ConfigProp prop) {
        super(bean, propertyDescriptor);
        propData = prop;
        this.name = propData.name();
        this.desc = propData.tooltip();
    }

    public ConfigProp getPropData() {
        return propData;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public String getOriginalName() {
        return propData.name();
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return this.desc;
    }

    public String getOriginalDescription() {
        return propData.tooltip();
    }

    public void setDescription(String desc) {
        this.desc = desc;
    }
}
