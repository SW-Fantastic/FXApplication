package org.swdc.fx.properties;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Language {

    private String languageLocation;

    private String displayName;

    private String name;

    @JsonIgnore
    private boolean loaded;

    @JsonIgnore
    private String assetsLocation;

    public void setAssetsLocation(String assetsLocation) {
        this.assetsLocation = assetsLocation;
    }

    private Map<String,String> textIdMap = new HashMap<>();

    public String getDisplayName() {
        return displayName;
    }

    public String getLanguageLocation() {
        return languageLocation;
    }


    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setLanguageLocation(String languageLocation) {
        this.languageLocation = languageLocation;
    }


    public void load() throws Exception {
        if (isLoaded()) {
            return;
        }
        File file = new File(assetsLocation + File.separator + this.languageLocation);
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().constructMapLikeType(HashMap.class,String.class,String.class);
        this.textIdMap = mapper.readValue(file,type);
        this.loaded = true;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public String getText(String textId) {
        return textIdMap.get(textId);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
