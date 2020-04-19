package org.swdc.fx.deploy.association;

import com.dd.plist.*;
import org.swdc.fx.deploy.AssociationManager;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OSXAssociationManager implements AssociationManager {

    @Override
    public boolean support() {
        String name = System.getProperty("os.name");
        if (!name.toLowerCase().contains("mac")) {
            return false;
        }
        File root = new File("");
        if (!root.getAbsolutePath().contains("app")) {
            // 启动环境不是Application bundle，因此不会有info.plist
            return false;
        }

        String parent = root.getAbsolutePath().split("[.]app")[0];
        File bundleData = new File(parent + ".app/Contents/info.plist");
        return bundleData.exists();
    }

    @Override
    public FileAssociation findAssociation(String extension) {
        File root = new File("");
        if (!root.getAbsolutePath().contains("app")) {
            // 启动环境不是Application bundle，因此不会有info.plist
            return null;
        }

        String parent = root.getAbsolutePath().split("[.]app")[0];
        File bundleData = new File(parent + ".app/Contents/info.plist");
        if (!bundleData.exists()) {
            return null;
        }
        try {
            NSDictionary data = (NSDictionary) PropertyListParser.parse(bundleData);
            if (!data.containsKey("CFBundleDocumentTypes")) {
                return null;
            }
            NSArray content = (NSArray)data.get("CFBundleDocumentTypes");
            NSObject[] array = content.getArray();
            if (array == null) {
                return null;
            }
            List<OSXFileAssociation> list = Stream.of(array)
                    .map(NSObject::toJavaObject)
                    .map(HashMap.class::cast)
                    .map(OSXFileAssociation::new)
                    .collect(Collectors.toList());

            return list.stream()
                    .filter(item -> item.getExtensions().contains(extension))
                    .findFirst()
                    .orElse(null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean writeAssociation(FileAssociation association) {
        FileAssociation assoc = findAssociation(association.getExtension());
        if (assoc != null) {
            return true;
        }
        try {
            File root = new File("");
            String parent = root.getAbsolutePath().split("[.]app")[0];
            File bundleData = new File(parent + ".app/Contents/info.plist");
            if (!bundleData.exists()) {
                return false;
            }
            NSDictionary data = (NSDictionary) PropertyListParser.parse(bundleData);
            if (!data.containsKey("CFBundleDocumentTypes")) {
                NSArray array = new NSArray(1);
                data.put("CFBundleDocumentTypes", array);
            }
            NSArray content = (NSArray)data.get("CFBundleDocumentTypes");
            NSDictionary dictionary = new NSDictionary();
            NSArray array = new NSArray(1);
            array.setValue(0, new NSString(association.getExtension()));

            dictionary.put(OSXFileAssociation.descKey,association.getDescription());
            dictionary.put(OSXFileAssociation.extnsionsKey,array);
            dictionary.put(OSXFileAssociation.bundleRoleKey,"Editor");
            dictionary.put(OSXFileAssociation.iconNameKey,association.getIconLocation());
            content.setValue(content.count() == 0 ? content.count() : content.count() - 1, dictionary);

            data.put("CFBundleDocumentTypes",content);
            PropertyListParser.saveAsXML(data,bundleData);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean removeAssociation(String extension) {
        File root = new File("");
        if (!root.getAbsolutePath().contains("app")) {
            // 启动环境不是Application bundle，因此不会有info.plist
            return false;
        }

        String parent = root.getAbsolutePath().split("[.]app")[0];
        File bundleData = new File(parent + ".app/Contents/info.plist");
        if (!bundleData.exists()) {
            return false;
        }
        try {
            NSDictionary data = (NSDictionary) PropertyListParser.parse(bundleData);
            if (!data.containsKey("CFBundleDocumentTypes")) {
                return false;
            }
            NSArray content = (NSArray) data.get("CFBundleDocumentTypes");
            NSObject[] array = content.getArray();
            if (array == null || array.length == 0) {
                return false;
            }
            for (int idx = 0; idx < array.length; idx++) {
                NSObject object = array[idx];
                OSXFileAssociation association = new OSXFileAssociation((HashMap) object.toJavaObject());
                if (association.getExtensions().contains(extension)) {
                    content.remove(idx);
                    data.put("CFBundleDocumentTypes", content);
                    PropertyListParser.saveAsXML(data,bundleData);
                    return true;
                }
            }
            return false;
        } catch (Exception e){

        }
        return false;
    }
}
