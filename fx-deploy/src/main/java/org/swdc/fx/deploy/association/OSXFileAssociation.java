package org.swdc.fx.deploy.association;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OSXFileAssociation extends FileAssociation {

    public static final String descKey = "CFBundleTypeName";
    public static final String extnsionsKey = "CFBundleTypeExtensions";
    public static final String iconNameKey = "CFBundleTypeIconFile";
    public static final String bundleRoleKey = "CFBundleTypeRole";

    private List<String> extensions;

    private String role;

    public OSXFileAssociation(HashMap data) {
        setDescription(data.containsKey(descKey) ? data.get(descKey).toString(): null);
        setIconLocation(data.containsKey(iconNameKey)? data.get(iconNameKey).toString():null);
        this.role = data.get(bundleRoleKey).toString();
        Object[] extensions = (Object[]) data.get(extnsionsKey);
        if (extensions != null) {
            this.extensions = Stream.of(extensions)
                    .map(String.class::cast)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public String getExtension() {
        return getExtensions().get(0);
    }

    public List<String> getExtensions() {
        return extensions;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
