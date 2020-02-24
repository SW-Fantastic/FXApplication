package org.swdc.fx.resource.source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.resource.Resource;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class ModulePathResource implements Resource {

    private Module module;
    private String path;
    private Boolean existed = null;

    private static Logger logger = LoggerFactory.getLogger(ModulePathResource.class);

    public ModulePathResource(Module module, String path) {
        this.module = module;
        this.path = path;
    }

    public ModulePathResource(String moduleName, String path) {
        this.module = ModuleLayer.boot().findModule(moduleName).orElse(null);
        if (module == null) {
            logger.error("can not find module: " + moduleName);
        }
    }

    @Override
    public InputStream getInputStream() {
        try {
            return this.module.getResourceAsStream(this.path);
        } catch (Exception ex) {
            logger.error("failed to open stream: " + path, ex);
            return null;
        }
    }

    @Override
    public OutputStream getOutputStream() {
        logger.error("can not write a resource in module path: " + path);
        return null;
    }

    @Override
    public File getFile() {
        logger.error("can not get a file from resource in module path: " + path);
        return null;
    }

    @Override
    public URL getURI() {
        logger.error("can not get uri from resource in module path: " + path);
        return null;
    }

    @Override
    public Boolean exist() {
        if (existed == null) {
            try(InputStream in = getInputStream()) {
                if (in != null) {
                    this.existed = true;
                }
                this.existed = false;
            } catch (Exception ex) {
                this.existed = false;
            }
        }
        return existed;
    }
}
