package org.swdc.fx.deploy.association;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.extern.ice.NoSuchKeyException;
import org.swdc.extern.ice.Registry;
import org.swdc.extern.ice.RegistryKey;
import org.swdc.fx.deploy.AssociationManager;

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

            WindowsAssociation windowsAssociation = new WindowsAssociation();
            RegistryKey assocKey = Registry.HKEY_CLASSES_ROOT.openSubKey(targetKey);

            RegistryKey iconKey = assocKey.openSubKey(WindowsAssociation.regIconKey);
            windowsAssociation.setIconLocation(iconKey.getDefaultValue());
            windowsAssociation.setExtension(extension);
            windowsAssociation.setDescription(assocKey.getDefaultValue());
            iconKey.closeKey();
            assocKey.closeKey();
            registryKey.closeKey();
            return windowsAssociation;
        } catch (NoSuchKeyException e) {
            return null;
        } catch (Exception ex) {
            logger.error("fail to lookup registry: ", ex);
            return null;
        }
    }

    @Override
    public boolean writeAssociation(FileAssociation assocation) {
        return false;
    }

}
