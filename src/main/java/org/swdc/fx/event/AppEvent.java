package org.swdc.fx.event;

import org.swdc.fx.AppComponent;

public class AppEvent<T> {

    private T data;
    private AppComponent source;

    public AppEvent(T data, AppComponent source) {
        this.data = data;
        this.source = source;
    }

    public T getData() {
        return data;
    }

    public AppComponent getSource() {
        return source;
    }
}
