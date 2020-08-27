package com.hkxx.drone.worker.template;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Template(name = "base")
public class BaseTemplate {

    public static final String LIST_ITEM_SEPARATOR = ",";

    public Map<String, String> addToMap(Map<String, String> map) {
        if (map == null) {
            map = new HashMap<>();
        }

        List<Field> fields = Arrays.asList(this.getClass().getFields());
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            Annotation annotation = field.getAnnotation(PlaceHolder.class);
            if (annotation != null) {
                PlaceHolder placeHolder = (PlaceHolder) annotation;
                if (placeHolder.value() != null) {
                    try {
                        Object value = field.get(this);
                        if (value instanceof String) {
                            map.put(placeHolder.value(), (String) value);
                        } else if (value instanceof List) {
                            List list = (List) value;
                            StringBuilder sb = new StringBuilder();
                            for (Object item : list) {
                                if (item instanceof BaseTemplate) {
                                    BaseTemplate baseTemplate = (BaseTemplate) item;
                                    String itemStr = TemplateHelper.loadTemplate(baseTemplate.getTemplateName(),
                                            baseTemplate.addToMap(null));
                                    if (itemStr != null) {
                                        sb.append(LIST_ITEM_SEPARATOR);
                                        sb.append(itemStr);
                                    }
                                }
                            }
                            if (sb.length() > 0) {
                                map.put(placeHolder.value(), sb.substring(LIST_ITEM_SEPARATOR.length()));
                            }
                        }
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return map;
    }

    public String getTemplateName() {
        Annotation annotation = this.getClass().getAnnotation(Template.class);
        if (annotation != null) {
            Template template = (Template) annotation;
            return template.name();
        }
        return null;
    }
}
