package org.swdc.fx.extra;

import javafx.scene.text.Font;

import java.util.HashMap;

public interface IconSPIService {

    enum IconSet {
        OPEN;
    }

    String getNamedIcon(IconSet icon);

    Font getFont();

}
