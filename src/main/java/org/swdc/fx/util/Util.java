package org.swdc.fx.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.swdc.fx.FXTheme;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Stream;

public class Util {

    public static String readStreamAsString(InputStream in) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))){
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Optional<ButtonType> showAlertDialog(Stage owner, String content, String title, Alert.AlertType type, FXTheme theme) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.initOwner(owner);
        try {
            theme.initFXView(alert.getDialogPane());
            return alert.showAndWait();
        }catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static byte[] intToByte(int val){
        byte[] b = new byte[4];
        b[0] = (byte)(val & 0xff);
        b[1] = (byte)((val >> 8) & 0xff);
        b[2] = (byte)((val >> 16) & 0xff);
        b[3] = (byte)((val >> 24) & 0xff);
        return b;
    }

    public static int byteToInt(byte[] bytes) {
        int value=0;
        for(int i = 0; i < 4; i++) {
            int shift= (3-i) * 8;
            value +=(bytes[i] & 0xFF) << shift;
        }
        return value;
    }


}
