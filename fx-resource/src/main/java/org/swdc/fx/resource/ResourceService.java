package org.swdc.fx.resource;

import org.swdc.fx.AppComponent;
import org.swdc.fx.resource.source.ClassPathResource;
import org.swdc.fx.resource.source.FileSystemResource;
import org.swdc.fx.resource.source.ModulePathResource;

public class ResourceService extends AppComponent {

    private String assetsPath = "";

    public Resource findResourceRelativeBy(Class clazz, String resourcePath) {
        Resource resource = null;
        if (clazz == null) {
            resource = new FileSystemResource(resourcePath);
            if (resource.exist()) {
                return resource;
            }
            return null;
        }
        resource = new ClassPathResource(clazz,resourcePath);
        if (resource.exist()) {
            return resource;
        }
        resource = new ModulePathResource(clazz.getModule(), resourcePath);
        if (resource.exist()) {
            return resource;
        }
        return null;
    }

    public void setAssetsPath(String assetsPath) {
        this.assetsPath = assetsPath;
    }
}
