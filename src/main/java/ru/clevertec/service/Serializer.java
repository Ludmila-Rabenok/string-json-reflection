package ru.clevertec.service;

import ru.clevertec.exception.JsonParserException;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Serializer {
    StringBuilder stringBuilder = new StringBuilder();

    public <T> String toJson(T t) {
        objectToJson(t);
        return stringBuilder.toString();
    }

    private <T> void objectToJson(T t) {
        stringBuilder.append("{");
        Class<?> tClass = t.getClass();
        Field[] allField = tClass.getDeclaredFields();
        Arrays.stream(allField)
                .forEach(field -> {
                            field.setAccessible(true);
                            fieldToJson(field, t);
                        }
                );
        int lastChar = stringBuilder.length() - 1;
        if (stringBuilder.charAt(lastChar) == ',') {
            stringBuilder.deleteCharAt(lastChar);
        }
        stringBuilder.append("}");
    }

    private <T> void fieldToJson(Field field, T t) {
        stringBuilder.append("\"").append(field.getName()).append("\":");
        Object value = getValueFromField(field, t);
        if (value == null) {
            stringBuilder.append("null");
        } else if (value instanceof Number) {
            stringBuilder.append(value);
        } else if (value instanceof OffsetDateTime) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSXXXXX");
            stringBuilder.append("\"")
                    .append(((OffsetDateTime) value).format(formatter))
                    .append("\"");
        } else if (value instanceof Collection<?>) {
            collectionToJson(value);
        } else if (value instanceof Map<?, ?>) {
            mapToJson(value);
        } else {
            stringBuilder.append("\"").append(value).append("\"");
        }
        stringBuilder.append(",");
    }

    private <T> Object getValueFromField(Field field, T t) {
        Object value = null;
        try {
            value = field.get(t);
        } catch (IllegalAccessException e) {
            throw new JsonParserException(e);
        }
        return value;
    }

    private <T> void mapToJson(Object value) {
        stringBuilder.append("{");
        Map<?, ?> map = (Map<?, ?>) value;
        if (map.isEmpty()) {
            stringBuilder.append("}");
            return;
        }
        map.entrySet()
                .forEach(entry -> {
                    entryToJson(entry);
                    stringBuilder.append(",");
                });
        int lastChar = stringBuilder.length() - 1;
        if (stringBuilder.charAt(lastChar) == ',') {
            stringBuilder.deleteCharAt(lastChar);
        }
        stringBuilder.append("}");
    }

    private void entryToJson(Map.Entry<?, ?> entry) {
        stringBuilder.append("\"");
        stringBuilder.append(entry.getKey());
        stringBuilder.append("\":\"");
        stringBuilder.append(entry.getValue());
        stringBuilder.append("\"");
    }

    private <T> void collectionToJson(Object value) {
        stringBuilder.append("[");
        List<?> list = (List<?>) value;
        if (list.isEmpty()) {
            stringBuilder.append("]");
            return;
        }
        list.forEach(o -> {
            objectToJson(o);
            stringBuilder.append(",");
        });
        int lastChar = stringBuilder.length() - 1;
        if (stringBuilder.charAt(lastChar) == ',') {
            stringBuilder.deleteCharAt(lastChar);
        }
        stringBuilder.append("]");
    }
}