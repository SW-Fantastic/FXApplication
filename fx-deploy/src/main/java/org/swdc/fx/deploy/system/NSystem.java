package org.swdc.fx.deploy.system;

import org.swdc.fx.deploy.system.impl.SystemSupportNative;

import java.io.File;

public class NSystem {

    /**
     * 安装一个系统模块
     * Windows：相当于Regsvr32命令，需要管理员权限。
     * macOS：尚未实现
     * @param moduleFile 文件
     * @return 是否成功
     */
    public static boolean installModule(File moduleFile) {
        return SystemSupportNative.installSystemModule(moduleFile.getAbsoluteFile().getAbsolutePath());
    }

    /**
     * 卸载一个系统模块
     * Windows：相当于Regsvr32 /u 命令，需要管理员权限。
     * macOS：尚未实现
     * @param moduleFile
     * @return
     */
    public static boolean uninstallModule(File moduleFile) {
        return SystemSupportNative.uninstallSystemModule(moduleFile.getAbsoluteFile().getAbsolutePath());
    }

    public static boolean is32Bits() {
        Integer bit = Integer.valueOf(System.getProperty("sun.arch.data.model"));
        return bit == 32;
    }

    public static boolean is64Bits() {
        Integer bit = Integer.valueOf(System.getProperty("sun.arch.data.model"));
        return bit == 64;
    }

}
