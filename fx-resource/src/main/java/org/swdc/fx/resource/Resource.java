package org.swdc.fx.resource;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public interface Resource {

    InputStream getInputStream();

    OutputStream getOutputStream();

    File getFile();

    URL getURI();

    Boolean exist();

}
