package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.Base64;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Options is a map of String keys and String values that can be easily
 * serialised and de-serialiezd.
 * <p>
 * The API is simlar to {@link Preferences}.
 */
public interface OptionsMap extends ReadOnlyOptionsMap, Map<String, String> {
    /**
     * Associates the specified key with the specified boolean value.
     *
     * @param key   a key
     * @param value a value
     * @see #getBoolean(String, boolean)
     */
    default void putBoolean(@NonNull String key, boolean value) {
        put(key, Boolean.toString(value));
    }

    default void putBoolean(@NonNull NonNullKey<Boolean> key, boolean value) {
        putBoolean(key.getName(), value);
    }

    /**
     * Associates the specified key with the specified byte array value.
     *
     * @param key   a key
     * @param value a value
     * @see #getByteArray(String, byte[])
     */
    default void putByteArray(@NonNull String key, byte @NonNull [] value) {
        put(key, Base64.getEncoder().encodeToString(value));
    }

    default void putByteArray(@NonNull NonNullKey<byte[]> key, byte @NonNull [] value) {
        putByteArray(key.getName(), value);
    }

    /**
     * Associates the specified key with the specified double value.
     *
     * @param key   a key
     * @param value a value
     * @see #getDouble(String, double)
     */
    default void putDouble(@NonNull String key, double value) {
        put(key, Double.toString(value));
    }

    default void putDouble(@NonNull NonNullKey<Double> key, double value) {
        putDouble(key.getName(), value);
    }

    /**
     * Associates the specified key with the specified float value.
     *
     * @param key   a key
     * @param value a value
     * @see #getFloat(String, float)
     */
    default void putFloat(@NonNull String key, float value) {
        put(key, Float.toString(value));
    }

    default void putFloat(@NonNull NonNullKey<Float> key, float value) {
        putFloat(key.getName(), value);
    }

    /**
     * Associates the specified key with the specified int value.
     *
     * @param key   a key
     * @param value a value
     * @see #getInt(String, int)
     */
    default void putInt(@NonNull String key, int value) {
        put(key, Integer.toString(value));
    }

    default void putInt(@NonNull NonNullKey<Integer> key, int value) {
        putInt(key.getName(), value);
    }

    /**
     * Associates the specified key with the specified long value.
     *
     * @param key   a key
     * @param value a value
     * @see #getLong(String, long)
     */
    default void putLong(@NonNull String key, long value) {
        put(key, Long.toString(value));
    }

    default void putLong(@NonNull NonNullKey<Long> key, long value) {
        putLong(key.getName(), value);
    }

    default void putString(@NonNull NonNullKey<String> key, @NonNull String value) {
        put(key.getName(), value);
    }

    /**
     * Puts values into the map from the specified String
     * <p>
     *
     * @param str a String
     * @return true on success
     * @see ReadOnlyOptionsMap#writeToString
     */
    default boolean readFromString(String str) {
        StreamTokenizer tt = new StreamTokenizer(new StringReader(str));
        tt.resetSyntax();
        tt.whitespaceChars(0, ' ');
        tt.quoteChar('"');
        try {
            while (tt.nextToken() != StreamTokenizer.TT_EOF) {
                if (tt.ttype != '"') {
                    return false;
                }
                String key = tt.sval;
                if (tt.nextToken() != ':') {
                    return false;
                }
                if (tt.nextToken() != '"') {
                    return false;
                }
                String value = tt.sval;
                if (tt.nextToken() != ',' && tt.ttype != StreamTokenizer.TT_EOF) {
                    return false;
                }
                put(key, value);
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
