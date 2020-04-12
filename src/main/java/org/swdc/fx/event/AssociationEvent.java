package org.swdc.fx.event;

import org.swdc.fx.AppComponent;
import org.swdc.fx.net.data.MainParameter;

public class AssociationEvent extends AppEvent<MainParameter> {

    public AssociationEvent(MainParameter data, AppComponent source) {
        super(data, source);
    }

}
