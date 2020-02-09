package org.swdc.fx.util;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Stream;

public class Util {

    public static String readAsString(File file) {
        try (FileInputStream fin = new FileInputStream(file)){
            return readStreamAsString(fin);
        } catch (Exception ex) {
            return null;
        }
    }

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

    public static List<Field> getClassFields(Class clazz) {
        ArrayList<Field> fields = new ArrayList<>();
        Class target = clazz;
        while (target != null && target != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            target = clazz.getSuperclass();
        }
        return fields;
    }

    public static Map<Class, Field> getClassTypeFieldMap(Class clazz) {
        HashMap<Class, Field> fields = new HashMap<>();
        Class target = clazz;
        while (target != null && target != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                fields.put(field.getType(), field);
            }
            target = target.getSuperclass();
        }
        return fields;
    }

}
