package org.swdc.fx.resource;

import javafx.scene.text.Font;
import org.swdc.fx.LifeCircle;
import org.swdc.fx.resource.icons.FontSize;

public abstract class IconFontService implements LifeCircle {

    public abstract String getFontIcon(String name);

    public abstract Font getFont(FontSize size);

}
