package com.hkxx.drone.worker.template;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class TemplateHelper {

    public static final String PREFIX_PROFILE = "template/";

    private static Map<String, String> templates = new HashMap<>();

    public static String loadTemplate(String templateName, Map<String, String> values) {
        String template = templates.get(templateName);
        if (template == null) {
            template = read(templateName);
            if (template == null) {
                return null;
            } else {
                templates.put(templateName, template);
            }
        }

        if (values != null) {
            for (String key : values.keySet()) {
                String value = values.get(key);
                if (value != null) {
                    template = template.replaceAll(key, value);
                }
            }
        }

        return template;
    }

    private static String read(String name) {
        if (name == null) {
            return null;
        }
        InputStream is = (TemplateHelper.class.getClassLoader().getResourceAsStream(PREFIX_PROFILE + name));
        byte[] bytes;
        try {
            bytes = new byte[is.available()];
            is.read(bytes);
            return new String(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
