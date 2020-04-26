package org.swdc.fx.deploy.system.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class SystemSupportNative {

    private static final Logger logger = LoggerFactory.getLogger(SystemSupportNative.class);

    static {
        try {
            String name = System.getProperty("os.name");
            String subFix = "";
            String libName = "SystemSupport";
            File support = null;
            if (name.toLowerCase().contains("mac")) {
                subFix = "dylib";
            } else if (name.toLowerCase().contains("windows")){
                libName = "libSystemSupport";
                subFix = "dll";
            } else if (name.toLowerCase().contains("linux")) {
                subFix = "so";
            }
            support = new File(libName + "." + subFix);
            if (!support.exists()) {
                Module module = SystemSupportNative.class.getModule();
                Integer bit = Integer.valueOf(System.getProperty("sun.arch.data.model"));
                InputStream in = module.getResourceAsStream("libSystemSupport_" + bit + "." + subFix);
                FileOutputStream outputStream = new FileOutputStream(new File("libSystemSupport."+subFix));
                in.transferTo(outputStream);
                outputStream.close();
            }
            System.loadLibrary(libName);
        } catch (Exception e) {
            logger.error("can not load native support lib",e);
        }
    }

    public static native String getProcessExecutablePath(long pid);

}
