package org.swdc.fx.resource;

import org.swdc.fx.extra.ExtraLoader;

public class ResourceLoader implements ExtraLoader {
    @Override
    public Class getModuleClass() {
        return ResourceModule.class;
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
