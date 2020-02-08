package org.swdc.fx;

import org.swdc.fx.anno.View;

import java.io.File;

public class FXTheme {

    private String name;

    public FXTheme(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void initView(FXView view) throws Exception {
        View viewProp = view.getClass().getAnnotation(View.class);
        if (viewProp.style().length == 1 && viewProp.style()[0].equals("")) {
            view.getStage()
                    .getScene()
                    .getStylesheets()
                    .add(new File("./assets/theme/" + this.name + "/stage.css").toURI().toURL().toExternalForm());
            File background = new File("./assets/theme/" + this.name + "/background.jpg");
            if (!background.exists()) {
                background = new File("./assets/theme/" + this.name + "/background.png");
            }
            if (background.exists()) {
                view.getStage()
                        .getScene()
                        .getRoot()
                        .setStyle("-fx-background-image: url(" + background.toURI().toURL().toExternalForm() + ");");
            }
        } else {
            String[] themes = viewProp.style();
            for (String style: themes) {
                view.getStage()
                        .getScene()
                        .getStylesheets()
                        .add(new File("./assets/theme/" + style + ".css").toURI().toURL().toExternalForm());
            }
        }
    }

}
