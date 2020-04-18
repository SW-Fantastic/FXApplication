package org.swdc.fx.deploy.association;

public class FileAssociation {

    private String extension;

    private String mimeType;

    private String iconLocation;

    private String description;

    public String getDescription() {
        return description;
    }

    public String getExtension() {
        return extension;
    }

    public String getIconLocation() {
        return iconLocation;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public void setIconLocation(String iconLocation) {
        this.iconLocation = iconLocation;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
