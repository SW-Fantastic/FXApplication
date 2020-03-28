package org.swdc.fx.event;

import org.swdc.fx.AppComponent;
import org.swdc.fx.properties.FXProperties;

public class ConfigRefreshEvent<T extends FXProperties> extends AppEvent<T>{

    public ConfigRefreshEvent(T data, AppComponent source) {
        super(data, source);
    }

}
