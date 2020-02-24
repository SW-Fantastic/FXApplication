package org.swdc.fx.resource.source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.resource.Resource;

import java.io.*;
import java.net.URL;

public class FileSystemResource implements Resource {

    private File file;
    private static Logger logger = LoggerFactory.getLogger(FileSystemResource.class);

    public FileSystemResource(File file) {
        this.file = file;
    }

    public FileSystemResource(String path) {
        this.file = new File(path);
    }

    @Override
    public InputStream getInputStream() {
        try {
            return new FileInputStream(file);
        } catch (Exception ex) {
            logger.error("can not open file resource: " + file.getAbsolutePath(), ex);
            return null;
        }
    }

    @Override
    public OutputStream getOutputStream() {
        try {
            return new FileOutputStream(file);
        } catch (Exception ex){
            logger.error("can not open stream of file :" + file.getAbsolutePath(), ex);
            return null;
        }
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public URL getURI() {
        try {
            return file.toURI().toURL();
        } catch (Exception ex) {
            logger.error("fail to build file url :" + file.getAbsolutePath());
            return null;
        }
    }

    @Override
    public Boolean exist() {
        return file.exists();
    }
}
