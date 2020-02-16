package org.swdc.fx.extra;

import org.swdc.fx.Container;

public abstract class ExtraModule<R> extends Container<R> implements Extra<R> {

    @Override
    public void setScope(Container<Container> scope) {
        super.setScope(scope);
    }

}
