package org.swdc.fx.jpa;

import org.swdc.fx.extra.ExtraLoader;

public class JPALoader implements ExtraLoader {
    @Override
    public Class getModuleClass() {
        return JPAExtraModule.class;
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
