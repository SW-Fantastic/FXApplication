package org.swdc.fx.net.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ExternalMessage {

    private byte[] data;

    private String clazzName;

    public ExternalMessage() {

    }

    public ExternalMessage(byte[] data, Class clazz) {
        this.data = data;
        this.clazzName = clazz.getName();
    }

    @JsonIgnore
    public Class getTargetClass() {
        try {
            return Class.forName(clazzName);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public byte[] getData() {
        return data;
    }

    public void setClazzName(String clazzName) {
        this.clazzName = clazzName;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getClazzName() {
        return clazzName;
    }
}
