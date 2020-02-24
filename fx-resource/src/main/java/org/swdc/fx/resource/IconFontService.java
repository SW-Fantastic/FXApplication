package org.swdc.fx.resource;

import javafx.scene.text.Font;
import org.swdc.fx.AppComponent;
import org.swdc.fx.resource.icons.FontSize;

public abstract class IconFontService extends AppComponent {

    public abstract void loadFontIcon();

    public abstract String getFontIcon(String name);

    public abstract Font getFont(FontSize size);

}
