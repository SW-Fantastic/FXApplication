package org.swdc.fx.extra;

import org.swdc.fx.container.Container;

public abstract class ExtraModule<R> extends Container<R> implements Extra<R> {

    @Override
    public void setParent(Container<Container> parent) {
        super.setParent(parent);
    }

}
