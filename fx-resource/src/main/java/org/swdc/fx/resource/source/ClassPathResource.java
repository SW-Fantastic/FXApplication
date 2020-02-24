package org.swdc.fx.resource.source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.resource.Resource;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public class ClassPathResource implements Resource {

    private Class clazz = null;
    private String path;

    private static Logger logger = LoggerFactory.getLogger(ClassPathResource.class);

    public ClassPathResource(Class clazz, String path) {
        this.clazz = clazz;
        this.path = path;
    }

    @Override
    public InputStream getInputStream() {
        try {
            InputStream stream = clazz.getResourceAsStream(path);
            if (stream == null) {
                stream = clazz.getModule().getResourceAsStream(path);
            }
            if (stream == null) {
                stream = clazz.getModule().getClassLoader().getResourceAsStream(path);
            }
            return stream;
        } catch (Exception ex) {
            logger.error("can not find class path resource: " + path, ex);
            return null;
        }
    }

    @Override
    public OutputStream getOutputStream() {
        logger.error("can not write class path resource");
        return null;
    }

    @Override
    public File getFile() {
        logger.error("can not resolve classpath resource to file");
        return null;
    }

    @Override
    public URL getURI() {
        try {
            URL url = clazz.getResource(path);
            if (url == null) {
                clazz.getClassLoader().getResource(path);
            }
            if (url == null) {
                url = clazz.getModule().getClassLoader().getResource(path);
            }
            return url;
        } catch (Exception ex) {
            logger.error("fail to resolve class path resource url: " + path, ex);
            return null;
        }
    }

    @Override
    public Boolean exist() {
        try(InputStream in = getInputStream()) {
            return in == null;
        } catch (Exception ex) {
            return false;
        }
    }
}
