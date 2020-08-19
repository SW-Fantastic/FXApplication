package org.swdc.fx.ux;

import javafx.scene.control.Label;
import org.swdc.fx.FXView;
import org.swdc.fx.anno.Scope;
import org.swdc.fx.anno.ScopeType;
import org.swdc.fx.anno.View;

@View(stage = false,value = "defaultView/MessageView.fxml")
@Scope(ScopeType.MULTI)
public class MessageView extends FXView {

    public void setText(String data) {
        Label label = findById("msgLbl");
        label.setText(data);
    }

}
