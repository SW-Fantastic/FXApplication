package org.swdc.fx.deploy.system;

import org.swdc.fx.deploy.system.impl.SystemSupportNative;

import java.util.List;

public class SystemProcess {

    public static String getProcessExecutablePath(long pid) {
        return SystemSupportNative.getProcessExecutablePath(pid);
    }

    public static List<SystemProc> getRunningProcesses() {
        return SystemSupportNative.getRunningProcesses();
    }

}
