package org.swdc.fx.deploy;

import org.swdc.fx.deploy.association.FileAssociation;
import org.swdc.fx.deploy.association.OSXAssociationManager;
import org.swdc.fx.deploy.association.WindowsAssociationManager;

public interface AssociationManager {

    static AssociationManager getAssociationManager() {
        String name = System.getProperty("os.name");
        if (name.toLowerCase().contains("mac")) {
            return new OSXAssociationManager();
        } else if (name.toLowerCase().contains("windows")){
            return new WindowsAssociationManager();
        }
        return null;
    }

    boolean support();

    FileAssociation findAssociation(String extension);

    boolean writeAssociation(FileAssociation assocation);

}
