package org.swdc.fx.deploy.association;

/**
 * windows注册表中，在HKEY_CLASSES_ROOT中记载文件关联
 * 以点+后缀名为Key，默认值是在HKEY_CLASSES_ROOT中正式记载
 * 文件关联的注册表项的Key。
 *
 * 例如 .txt 是HKEY_CLASSES_ROOT里面的一个记录，它记载的值是txt_file，
 * 那么在HKEY_CLASSES_ROOT里面就一定存在txt_file记载如何打开.txt文件。
 */
public class WindowsFileAssociation extends FileAssociation {

    public static final String regExtensionPreFix = "Standard.FXApplication.";
    public static final String regIconKey = "DefaultIcon";
    public static final String regShell = "shell";
    public static final String regShellOpen = "open";
    public static final String regShellOpenCommand = "command";


    public WindowsFileAssociation() {

    }

    public String getRegTypeKey() {
        return regExtensionPreFix + getExtension();
    }

}
