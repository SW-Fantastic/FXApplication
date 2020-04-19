package org.swdc.fx.deploy.association;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.extern.ice.NoSuchKeyException;
import org.swdc.extern.ice.RegStringValue;
import org.swdc.extern.ice.Registry;
import org.swdc.extern.ice.RegistryKey;
import org.swdc.fx.deploy.AssociationManager;
import org.swdc.fx.deploy.system.SystemProcess;

import java.io.File;
import java.lang.management.ManagementFactory;

public class WindowsAssociationManager implements AssociationManager {

    private static final Logger logger = LoggerFactory.getLogger(WindowsAssociationManager.class);

    @Override
    public boolean support() {
        String name = System.getProperty("os.name");
        if (name.toLowerCase().contains("windows")) {
            return true;
        }
        return false;
    }

    @Override
    public FileAssociation findAssociation(String extension) {
        try {
            RegistryKey registryKey = Registry.HKEY_CLASSES_ROOT.openSubKey("." + extension.toLowerCase());
            String targetKey = registryKey.getDefaultValue();

            WindowsFileAssociation windowsFileAssociation = new WindowsFileAssociation();
            RegistryKey assocKey = Registry.HKEY_CLASSES_ROOT.openSubKey(targetKey);

            RegistryKey iconKey = assocKey.openSubKey(WindowsFileAssociation.regIconKey);
            windowsFileAssociation.setIconLocation(iconKey.getDefaultValue());
            windowsFileAssociation.setExtension(extension);
            windowsFileAssociation.setDescription(assocKey.getDefaultValue());
            iconKey.closeKey();
            assocKey.closeKey();
            registryKey.closeKey();
            return windowsFileAssociation;
        } catch (NoSuchKeyException e) {
            return null;
        } catch (Exception ex) {
            logger.error("fail to lookup registry: ", ex);
            return null;
        }
    }

    @Override
    public boolean writeAssociation(FileAssociation assocation) {
        if (findAssociation(assocation.getExtension()) != null) {
            return true;
        }
        try {
            RegistryKey assocKey = Registry.HKEY_CLASSES_ROOT.createSubKey("." + assocation.getExtension(), "");
            assocKey.setValue(new RegStringValue(assocKey, "", WindowsFileAssociation.regExtensionPreFix + assocation.getExtension()));
            assocKey.closeKey();
            RegistryKey target = Registry.HKEY_CLASSES_ROOT.createSubKey(WindowsFileAssociation.regExtensionPreFix + assocation.getExtension(),"");
            target.setValue(new RegStringValue(target,"",assocation.getDescription()));

            RegistryKey icon = target.createSubKey(WindowsFileAssociation.regIconKey,"");
            File iconFile = new File(assocation.getIconLocation());
            icon.setValue(new RegStringValue(icon,"",iconFile.getAbsolutePath()));
            icon.closeKey();

            RegistryKey shell = target.createSubKey(WindowsFileAssociation.regShell, "");
            RegistryKey open = shell.createSubKey(WindowsFileAssociation.regShellOpen, "");
            RegistryKey command = open.createSubKey(WindowsFileAssociation.regShellOpenCommand, "");
            long pid = ManagementFactory.getRuntimeMXBean().getPid();
            String path = SystemProcess.getProcessExecutablePath(pid);
            command.setValue(new RegStringValue(command,"","\""+path+"\" \"%1\""));
            command.closeKey();
            open.closeKey();
            shell.closeKey();
            return true;
        } catch (Exception e) {
            logger.error("fail to create windows association", e);
            return false;
        }
    }

    @Override
    public boolean removeAssociation(String extension) {
        if (findAssociation(extension) == null) {
            return true;
        }
        try {
            RegistryKey registryKey = Registry.HKEY_CLASSES_ROOT.openSubKey("." + extension.toLowerCase());
            String targetKey = registryKey.getDefaultValue();

            Registry.HKEY_CLASSES_ROOT.deleteSubKey(targetKey);
            Registry.HKEY_CLASSES_ROOT.deleteSubKey(registryKey.getName());
            return true;
        } catch (Exception e) {
            logger.error("fail to remove association",e);
            return false;
        }
    }

}
