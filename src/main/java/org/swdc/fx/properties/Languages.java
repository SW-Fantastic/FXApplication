package org.swdc.fx.properties;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.swdc.fx.services.Service;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Languages extends Service {

    private Map<String,Language> languageMap = new HashMap<>();

    public static final String LANGUAGE_PREFIX = "lang@";

    @Override
    public void initialize() {
        File languages = new File(getAssetsPath() + "/lang.json");
        if (!languages.exists()) {
            return;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            JavaType mapType = mapper.getTypeFactory().constructMapType(HashMap.class,String.class,Language.class);
            languageMap = mapper.readValue(languages,mapType);
        } catch (Exception e) {
            logger.error("fail to load languages");
        }
    }

    public String getLanguage(String textId) {
        DefaultUIConfigProp configProp = findComponent(DefaultUIConfigProp.class);
        String language = configProp.getLanguage();
        if (languageMap.containsKey(language)) {
            Language item = languageMap.get(configProp.getLanguage());
            item.setAssetsLocation(getAssetsPath());
            try {
                if (!item.isLoaded()) {
                    item.load();
                }
                return item.getText(textId);
            } catch (Exception e) {
                logger.error("无法加载翻译文件：" + item.getLanguageLocation(),e);
            }
        }
        return "无i18n翻译";
    }

}
