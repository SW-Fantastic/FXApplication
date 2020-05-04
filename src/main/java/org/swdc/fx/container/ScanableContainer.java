package org.swdc.fx.container;

import org.swdc.fx.AppComponent;
import org.swdc.fx.scanner.IPackageScanner;

import java.util.List;

public abstract class ScanableContainer<T extends AppComponent> extends Container<T> {

    @Override
    public void initialize() {
        IPackageScanner scanner = IPackageScanner.getScanner(this.getClass());
        List<Class<?>> classList = scanner.scanPackage();
        for (Class clazz: classList) {
            if (this.isComponentOf(clazz)) {
                register(clazz);
            }
        }
    }
}
