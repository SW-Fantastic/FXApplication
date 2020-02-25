package org.swdc.fx.resource;

import javafx.scene.text.Font;
import org.swdc.fx.LifeCircle;
import org.swdc.fx.resource.icons.FontSize;

import java.util.Set;

public interface IconFontService extends LifeCircle {

    String getFontIcon(String name);

    Font getFont(FontSize size);

    Set<String> getIconNames();

}
