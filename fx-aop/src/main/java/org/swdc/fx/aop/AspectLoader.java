package org.swdc.fx.aop;

import org.swdc.fx.extra.ExtraLoader;

public class AspectLoader implements ExtraLoader {
    @Override
    public Class getModuleClass() {
        return AspectExtraModule.class;
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
