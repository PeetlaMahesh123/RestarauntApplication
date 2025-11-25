package com.restaurant.app.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal JSON parser / serializer to avoid external dependencies.
 * Supports objects, arrays, strings, numbers, booleans, and null.
 */
public final class JsonUtil {

    private JsonUtil() {
    }

    public static Object parse(String json) {
        if (json == null) {
            return null;
        }
        Parser parser = new Parser(json.trim());
        Object value = parser.parseValue();
        parser.skipWhitespace();
        if (!parser.isEnd()) {
            throw new IllegalArgumentException("Unexpected trailing data in JSON payload");
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        Object obj = parse(json);
        if (!(obj instanceof Map)) {
            throw new IllegalArgumentException("JSON payload is not an object");
        }
        return (Map<String, Object>) obj;
    }

    public static String stringify(Object value) {
        StringBuilder sb = new StringBuilder();
        writeValue(value, sb);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void writeValue(Object value, StringBuilder sb) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof String str) {
            sb.append('"').append(escape(str)).append('"');
        } else if (value instanceof Number || value instanceof Boolean) {
            sb.append(value);
        } else if (value instanceof Map<?, ?> map) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                sb.append('"').append(escape(String.valueOf(entry.getKey()))).append('"').append(':');
                writeValue(entry.getValue(), sb);
            }
            sb.append('}');
        } else if (value instanceof List<?> list) {
            sb.append('[');
            boolean first = true;
            for (Object item : list) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                writeValue(item, sb);
            }
            sb.append(']');
        } else {
            sb.append('"').append(escape(String.valueOf(value))).append('"');
        }
    }

    private static String escape(String value) {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    private static final class Parser {
        private final String json;
        private int index;

        Parser(String json) {
            this.json = json;
        }

        boolean isEnd() {
            return index >= json.length();
        }

        void skipWhitespace() {
            while (!isEnd()) {
                char ch = json.charAt(index);
                if (Character.isWhitespace(ch)) {
                    index++;
                } else {
                    break;
                }
            }
        }

        Object parseValue() {
            skipWhitespace();
            if (isEnd()) {
                throw new IllegalArgumentException("Unexpected end of JSON input");
            }
            char ch = json.charAt(index);
            return switch (ch) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 't', 'f' -> parseBoolean();
                case 'n' -> parseNull();
                default -> parseNumber();
            };
        }

        Map<String, Object> parseObject() {
            expect('{');
            Map<String, Object> map = new LinkedHashMap<>();
            skipWhitespace();
            if (peek('}')) {
                index++;
                return map;
            }
            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                Object value = parseValue();
                map.put(key, value);
                skipWhitespace();
                if (peek('}')) {
                    index++;
                    break;
                }
                expect(',');
            }
            return map;
        }

        List<Object> parseArray() {
            expect('[');
            List<Object> list = new ArrayList<>();
            skipWhitespace();
            if (peek(']')) {
                index++;
                return list;
            }
            while (true) {
                Object value = parseValue();
                list.add(value);
                skipWhitespace();
                if (peek(']')) {
                    index++;
                    break;
                }
                expect(',');
            }
            return list;
        }

        String parseString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (!isEnd()) {
                char ch = json.charAt(index++);
                if (ch == '"') {
                    break;
                }
                if (ch == '\\') {
                    if (isEnd()) {
                        throw new IllegalArgumentException("Invalid escape sequence");
                    }
                    char esc = json.charAt(index++);
                    sb.append(switch (esc) {
                        case '"' -> '"';
                        case '\\' -> '\\';
                        case '/' -> '/';
                        case 'b' -> '\b';
                        case 'f' -> '\f';
                        case 'n' -> '\n';
                        case 'r' -> '\r';
                        case 't' -> '\t';
                        case 'u' -> parseUnicode();
                        default -> throw new IllegalArgumentException("Unknown escape: \\" + esc);
                    });
                } else {
                    sb.append(ch);
                }
            }
            return sb.toString();
        }

        char parseUnicode() {
            if (index + 4 > json.length()) {
                throw new IllegalArgumentException("Incomplete unicode escape");
            }
            String hex = json.substring(index, index + 4);
            index += 4;
            return (char) Integer.parseInt(hex, 16);
        }

        Object parseBoolean() {
            if (json.startsWith("true", index)) {
                index += 4;
                return Boolean.TRUE;
            }
            if (json.startsWith("false", index)) {
                index += 5;
                return Boolean.FALSE;
            }
            throw new IllegalArgumentException("Invalid boolean literal");
        }

        Object parseNull() {
            if (json.startsWith("null", index)) {
                index += 4;
                return null;
            }
            throw new IllegalArgumentException("Invalid null literal");
        }

        Number parseNumber() {
            int start = index;
            if (peek('-')) {
                index++;
            }
            while (!isEnd() && Character.isDigit(json.charAt(index))) {
                index++;
            }
            if (!isEnd() && json.charAt(index) == '.') {
                index++;
                while (!isEnd() && Character.isDigit(json.charAt(index))) {
                    index++;
                }
            }
            if (!isEnd() && (json.charAt(index) == 'e' || json.charAt(index) == 'E')) {
                index++;
                if (!isEnd() && (json.charAt(index) == '+' || json.charAt(index) == '-')) {
                    index++;
                }
                while (!isEnd() && Character.isDigit(json.charAt(index))) {
                    index++;
                }
            }
            String number = json.substring(start, index);
            if (number.contains(".") || number.contains("e") || number.contains("E")) {
                return Double.parseDouble(number);
            }
            try {
                return Integer.parseInt(number);
            } catch (NumberFormatException ex) {
                return Long.parseLong(number);
            }
        }

        boolean peek(char expected) {
            return !isEnd() && json.charAt(index) == expected;
        }

        void expect(char expected) {
            if (isEnd() || json.charAt(index) != expected) {
                throw new IllegalArgumentException("Expected '" + expected + "' but found '" + (isEnd() ? "EOF" : json.charAt(index)) + "'");
            }
            index++;
        }
    }
}


