package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Base64;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Options is a map of String keys and String values that can be easily
 * serialised and de-serialiezd.
 * <p>
 * The API is simlar to {@link Preferences}.
 */
public interface ReadOnlyOptionsMap extends ReadOnlyMap<String, String> {
    /**
     * Returns the boolean value represented by the string associated
     * with the specified key, or the specified default value.
     * <p>
     * Valid strings are:
     * <ul>
     *     <li>{@code "true"}</li>
     *     <li>{@code "false"}</li>
     * </ul>
     *
     * @param key the key
     * @param def the default value to be returned if the map does not contain
     *            a valid value
     * @return the associated value
     */
    default boolean getBoolean(@NonNull String key, boolean def) {
        String s = get(key);
        if (s != null) {
            switch (s) {
            case "true":
                return true;
            case "false":
                return false;
            }
        }
        return def;
    }

    /**
     * Returns the byte array value represented by the string associated
     * with the specified key, or the specified default value.
     * <p>
     * Valid strings are {@link Base64} encoded binary data.
     *
     * @param key the key
     * @param def the default value to be returned if the map does not contain
     *            a valid value
     * @return the associated value
     */
    default byte[] getByteArray(@NonNull String key, byte @NonNull [] def) {
        String s = get(key);
        if (s != null) {
            try {
                return Base64.getDecoder().decode(s);
            } catch (IllegalArgumentException e) {
                // we have to return def
            }
        }
        return def;
    }

    /**
     * Returns the double value represented by the string associated
     * with the specified key, or the specified default value.
     * <p>
     * Valid strings are values that {@link Double#parseDouble(String)} can parse.
     *
     * @param key the key
     * @param def the default value to be returned if the map does not contain
     *            a valid value
     * @return the associated value
     */
    default double getDouble(@NonNull String key, double def) {
        String s = get(key);
        if (s != null) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                // we have to return def
            }
        }
        return def;
    }

    /**
     * Returns the float value represented by the string associated
     * with the specified key, or the specified default value.
     * <p>
     * Valid strings are values that {@link Float#parseFloat(String)} can parse.
     *
     * @param key the key
     * @param def the default value to be returned if the map does not contain
     *            a valid value
     * @return the associated value
     */
    default float getFloat(@NonNull String key, float def) {
        String s = get(key);
        if (s != null) {
            try {
                return Float.parseFloat(s);
            } catch (NumberFormatException e) {
                // we have to return def
            }
        }
        return def;
    }


    /**
     * Returns the int value represented by the string associated
     * with the specified key, or the specified default value.
     * <p>
     * Valid strings are values that {@link Integer#parseInt(String)} can parse.
     *
     * @param key the key
     * @param def the default value to be returned if the map does not contain
     *            a valid value
     * @return the associated value
     */
    default int getInt(@NonNull String key, int def) {
        String s = get(key);
        if (s != null) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                // we have to return def
            }
        }
        return def;
    }


    /**
     * Returns the int value represented by the string associated
     * with the specified key, or the specified default value.
     * <p>
     * Valid strings are values that {@link Long#parseLong(String)} can parse.
     *
     * @param key the key
     * @param def the default value to be returned if the map does not contain
     *            a valid value
     * @return the associated value
     */
    default long getLong(@NonNull String key, long def) {
        String s = get(key);
        if (s != null) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                // we have to return def
            }
        }
        return def;
    }

    /**
     * Returns the String value represented by the string associated
     * with the specified key, or the specified default value.
     *
     * @param key the key
     * @param def the default value to be returned if the map does not contain
     *            a valid value
     * @return the associated value
     */
    default String get(@NonNull String key, @NonNull String def) {
        String s = get(key);
        if (s != null) {
            return s;
        }
        return def;
    }

    /**
     * Writes the map to a String.
     * <p>
     * Syntax:
     * <pre>
     * Map = Entry, ("\n,", Entry) * ;
     * Entry = Key, ":", Value ;
     * Key = String;
     * Value = String;
     * String = '"', EscapedStringChar*, '"';
     * EscapedStringChar = UnicodeChar - EvilStringChar | '\' EvilStringChar ;
     * EvilStringChar = '\n' | '\r' | '"';
     * </pre>
     * Example:
     * <pre>
     * "key1":"value1",
     * "key2":"value\nwith line break",
     * "key3":"value3"
     * </pre>
     *
     * @return a String
     */
    default String writeToString() {
        StringBuilder buf = new StringBuilder();
        for (Map.Entry<String, String> entry : readOnlyEntrySet()) {
            if (buf.length() != 0) {
                buf.append(",\n");
            }
            escapeString(buf, entry.getKey());
            buf.append(':');
            escapeString(buf, entry.getValue());
        }
        return buf.toString();
    }

    private static void escapeString(StringBuilder buf, String str) {
        buf.append('"');
        int start = 0;
        int end = indexOfBadChar(str, 0);
        while (end != -1) {
            buf.append(str, start, end);
            buf.append('\\');
            start = end;
            end = indexOfBadChar(str, end + 1);
        }
        buf.append(str, start, str.length());
        buf.append('"');
    }

    private static int indexOfBadChar(String str, int start) {
        for (int i = start, n = str.length(); i < n; i++) {
            switch (str.charAt(i)) {
            case '\r':
            case '\n':
            case '"':
                return i;
            }
        }
        return -1;
    }

    default double getDouble(NonNullKey<Double> key) {
        return getDouble(key.getName(), key.getDefaultValue());
    }

    default boolean getBoolean(NonNullKey<Boolean> key) {
        return getBoolean(key.getName(), key.getDefaultValue());
    }

}
