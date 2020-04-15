package org.swdc.fx.resource.source;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.fx.resource.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ArchiveFileResource implements Resource {

    private File archive;
    private URI archiveURI;
    private String path;

    private static Logger logger = LoggerFactory.getLogger(ArchiveFileResource.class);

    public ArchiveFileResource(File file, String path) {
        this.archive = file;
        this.path = path;
        this.archiveURI = getVirtualURI(file);
    }

    public static URI getVirtualURI(File file) {
        try {
            return URI.create("jar:" + file.getAbsoluteFile().toURI().toURL().toExternalForm());
        } catch (Exception e) {
            return null;
        }
    }

    public static FileSystem createAFS(URI uri) throws IOException {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        return FileSystems.newFileSystem(uri,env);
    }

    @Override
    public InputStream getInputStream() {
        try(FileSystem fs = createAFS(archiveURI)) {
            return Files.newInputStream(fs.getPath(path));
        } catch (Exception ex) {
            logger.error("can not open input stream",ex);
            return null;
        }
    }

    @Override
    public OutputStream getOutputStream() {
        try(FileSystem fs = createAFS(archiveURI)) {
            return Files.newOutputStream(fs.getPath(path));
        } catch (Exception ex) {
            logger.error("can not open output stream",ex);
            return null;
        }
    }

    @Override
    public File getFile() {
        return archive;
    }

    @Override
    public URL getURI() {
        try (FileSystem fs = createAFS(archiveURI)){
            return fs.getPath(path).toUri().toURL();
        } catch (Exception ex) {
            logger.error("can not resolve to url", ex);
            return null;
        }
    }

    @Override
    public Boolean exist() {
        try (FileSystem fs = createAFS(archiveURI)){
            return Files.exists(fs.getPath(path));
        } catch (Exception e) {
            logger.error("fail to lookup path: " + path, e);
            return false;
        }
    }
}
