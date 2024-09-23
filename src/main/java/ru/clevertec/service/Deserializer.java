package ru.clevertec.service;

import ru.clevertec.exception.JsonParserException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Deserializer {

    public <T> T toObject(String json, Class<T> clazz) {
        Map<String, Object> map = getMapFromJson(json);
        return createObject(map, clazz);
    }

    private <T> T createObjectFromMap(Map<String, Object> map, Class<T> clazz) {
        Constructor<T> constructor = null;
        T t = null;
        try {
            constructor = clazz.getConstructor();
            t = constructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new JsonParserException(e);
        }
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            String nameField = field.getName();
            if (map.containsKey(nameField)) {
                Object value = castToType(map.get(nameField), field.getGenericType().getClass());
                try {
                    field.set(t, value);
                } catch (IllegalAccessException e) {
                    throw new JsonParserException(e);
                }
            }
        }
        return t;
    }

    private <T> T createObject(Object o, Class<T> clazz) {
        if (o == null) {
            return null;
        }
        if (o instanceof Map) {
            return createObjectFromMap((Map<String, Object>) o, clazz);
        }
        throw new JsonParserException();
    }

    private Object castToType(Object value, Class<?> type) {
        if (value == null) {
            return null;
        }
        if (type == String.class) {
            return value.toString();
        } else if (type == Integer.class || type == int.class) {
            return Integer.parseInt(value.toString());
        } else if (type == UUID.class) {
            return UUID.fromString(value.toString());
        } else if (type == Double.class || type == double.class) {
            return Double.parseDouble(value.toString());
        } else if (type == LocalDate.class) {
            return LocalDate.parse(value.toString());
        } else if (type == OffsetDateTime.class) {
            return OffsetDateTime.parse(value.toString());
        } else if (type == List.class) {
            return listCastToType((List<?>) value, type);
        } else if (type == Map.class) {
            return mapCastToType((Map<?, ?>) value, type);
        }
        return createObject(value, type);
    }

    private List<Object> listCastToType(List<?> list, Type type) {
        Type actualTypeArgument = Object.class;
        if (type instanceof ParameterizedType pType) {
            actualTypeArgument = pType.getActualTypeArguments()[0];
        }
        List<Object> castedList = new ArrayList<>();
        for (Object o : list) {
            castedList.add(castToType(o, actualTypeArgument.getClass()));
        }
        return castedList;
    }

    private Map<Object, Object> mapCastToType(Map<?, ?> map, Type type) {
        Type actualTypeArgumentKey = Object.class;
        Type actualTypeArgumentValue = Object.class;
        if (type instanceof ParameterizedType pType) {
            actualTypeArgumentKey = pType.getActualTypeArguments()[0];
            actualTypeArgumentValue = pType.getActualTypeArguments()[1];
        }
        Map<Object, Object> castedMap = new HashMap<>();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object key = castToType(entry.getKey(), actualTypeArgumentKey.getClass());
            Object value = castToType(entry.getValue(), actualTypeArgumentValue.getClass());
            castedMap.put(key, value);
        }
        return castedMap;
    }

    private Map<String, Object> getMapFromJson(String json) {
        Map<String, Object> jsonMap = new LinkedHashMap<>();
        if (json.startsWith("{")) {
            jsonMap = parseObject(json);
        }
        return jsonMap;
    }

    private Map<String, Object> parseObject(String json) {
        Map<String, Object> map = new LinkedHashMap<>();
        json = json.substring(1, json.length() - 1).trim();
        List<String> fields = splitJsonOnFields(json);
        for (String field : fields) {
            String[] array = field.split(":", 2);
            String key = removeQuotes(array[0]);
            Object value = parseValue(array[1].trim());
            map.put(key, value);
        }
        return map;
    }

    private List<String> splitJsonOnFields(String json) {
        List<String> fields = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        boolean isQuotes = false;
        int nesting = 0;
        for (char ch : json.toCharArray()) {
            if (ch == '"') {
                isQuotes = !isQuotes;
            } else if (!isQuotes) {
                if (ch == '{' || ch == '[') {
                    nesting++;
                } else if (ch == '}' || ch == ']') {
                    nesting--;
                } else if (ch == ',' && nesting == 0) {
                    fields.add(builder.toString().trim());
                    builder.delete(0, builder.length());
                    continue;
                }
            }
            builder.append(ch);
        }
        if (builder.isEmpty()) {
            return fields;
        } else
            fields.add(builder.toString());
        return fields;
    }

    private Object parseValue(String json) {
        if (json == null) {
            return null;
        }
        json = json.trim();
        if (json.startsWith("{")) {
            return parseObject(json);
        } else if (json.startsWith("[")) {
            return parseCollection(json);
        } else if (json.startsWith("\"")) {
            return json.substring(1, json.length() - 1);
        } else return json;
    }

    private List<Object> parseCollection(String json) {
        List<Object> list = new ArrayList<>();
        json = json.substring(1, json.length() - 1).trim();
        List<String> fields = splitJsonOnFields(json);
        for (String field : fields) {
            list.add(parseValue(field));
        }
        return list;
    }

    private String removeQuotes(String value) {
        if (value == null) {
            return value;
        } else if (value.startsWith("\"")) {
            value = value.substring(1);
        } else if (value.endsWith("\"")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}
