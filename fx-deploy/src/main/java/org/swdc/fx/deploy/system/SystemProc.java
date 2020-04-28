package org.swdc.fx.deploy.system;

import org.swdc.fx.deploy.system.impl.SystemSupportNative;

public class SystemProc {

    private String name;
    private Long pid;
    private String path;

    public SystemProc() {

    }

    public SystemProc(String name, String path, long pid) {
        this.name = name;
        this.path = path;
        this.pid = pid;
    }

    public Long getPid() {
        return pid;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public boolean stop() {
        return SystemSupportNative.stopProcess(this.pid);
    }

    @Override
    public String toString() {
        return "[pid]: " + pid + "\t  [name]:" + name;
    }
}
