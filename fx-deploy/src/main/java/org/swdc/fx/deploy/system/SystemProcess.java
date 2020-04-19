package org.swdc.fx.deploy.system;

import org.swdc.fx.deploy.system.impl.SystemSupportNative;

public class SystemProcess {

    public static String getProcessExecutablePath(long pid) {
        return SystemSupportNative.getProcessExecutablePath(pid);
    }

}
