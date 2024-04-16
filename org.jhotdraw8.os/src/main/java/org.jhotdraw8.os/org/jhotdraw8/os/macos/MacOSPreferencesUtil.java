/*
 * @(#)MacOSPreferencesUtil.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.os.macos;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides read methods for some well known macOS preferences files.
 */
public class MacOSPreferencesUtil {
    /**
     * Path to global preferences.
     */
    public static final @NonNull File GLOBAL_PREFERENCES = new File(System.getProperty("user.home"), "Library/Preferences/.GlobalPreferences.plist");
    /**
     * Path to finder preferences.
     */
    public static final @NonNull File FINDER_PREFERENCES = new File(System.getProperty("user.home"), "Library/Preferences/com.apple.finder.plist");
    /**
     * Each entry in this hash map represents a cached preferences file.
     */
    private static ConcurrentHashMap<File, Map<String, Object>> cachedFiles;

    /**
     * Creates a new instance.
     */
    public MacOSPreferencesUtil() {
    }

    public static @Nullable String getString(@NonNull File file, @NonNull String key) {
        return (String) get(file, key);
    }

    public static @NonNull String getString(@NonNull File file, String key, String defaultValue) {
        return (String) get(file, key, defaultValue);
    }

    public static boolean isStringEqualTo(@NonNull File file, String key, String defaultValue, String compareWithThisValue) {
        return get(file, key, defaultValue).equals(compareWithThisValue);
    }

    /**
     * Gets a preferences value
     *
     * @param file the preferences file
     * @param key  the key may contain tabulator separated entries to directly access a value in a sub-dictionary
     * @return the value associated with the key
     */
    public static @Nullable Object get(@NonNull File file, @NonNull String key) {
        ensureCached(file);
        final Map<String, Object> map = cachedFiles.get(file);
        return map == null ? null : get(map, key);
    }

    @SuppressWarnings("unchecked")
    public static @NonNull Map<String, Object> flatten(@NonNull Map<String, Object> map) {
        LinkedHashMap<String, Object> flattened = new LinkedHashMap<>();
        final Object plist = map.get("plist");
        if (plist instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Map) {
                    flattened.putAll((Map<String, Object>) o);
                }
            }
        }
        return flattened;
    }

    public static @Nullable Object get(@NonNull Map<String, Object> map, @NonNull String key) {
        final String[] split = key.split("\t");
        final Object plist = map.get("plist");
        if (plist instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> m = (Map<String, Object>) o;
                    for (int i = 0, n = split.length; i < n; i++) {
                        String subkey = split[i];
                        Object value;
                        if (m.containsKey(subkey)) {
                            value = m.get(subkey);
                            if (i < n - 1 && (value instanceof Map)) {
                                @SuppressWarnings("unchecked")
                                Map<String, Object> unchecked = (Map<String, Object>) value;
                                m = unchecked;
                            } else if (i == n - 1) {
                                return value;
                            }
                        }
                    }

                }
            }
        }
        return null;
    }

    /**
     * Returns all known keys for the specified preferences file.
     *
     * @return
     */
    public static @NonNull Set<String> getKeySet(@NonNull File file) {
        ensureCached(file);
        return cachedFiles.get(file).keySet();
    }

    /**
     * Clears all caches.
     */
    public static void clearAllCaches() {
        cachedFiles.clear();

    }

    /**
     * Clears the cache for the specified preference file.
     */
    public static void clearCache(File f) {
        cachedFiles.remove(f);
    }

    /**
     * Get a value from a Mac OS X preferences file.
     *
     * @param file         The preferences file.
     * @param key          Hierarchical keys are separated by \t characters.
     * @param defaultValue This value is returned when the key does not exist.
     * @return Returns the preferences value.
     */
    public static Object get(@NonNull File file, String key, Object defaultValue) {
        ensureCached(file);
        return cachedFiles.get(file).getOrDefault(key, defaultValue);
    }

    private static void ensureCached(@NonNull File file) {
        if (cachedFiles == null) {
            cachedFiles = new ConcurrentHashMap<>();
        }
        if (!cachedFiles.containsKey(file)) {
            Map<String, Object> cache = new HashMap<>();
            try {
                FileTime lastModifiedTime = Files.getLastModifiedTime(file.toPath());
                cache.put("lastModifiedTime", lastModifiedTime);
            } catch (IOException e) {
                //we failed to determine the last modified time
            }
            readPreferences(file, cache);
            cachedFiles.put(file, Map.copyOf(cache));
        }
    }

    public static boolean isMacOs() {
        final String osName = System.getProperty("os.name");
        return osName != null && osName.toLowerCase().startsWith("mac");
    }

    public static void readPreferences(@NonNull File file, @NonNull Map<String, Object> cache) {
        cache.clear();

        if (isMacOs()) {
            try {
                Document plist = PListParsers.readPList(file);
                cache.putAll(PListParsers.toMap(plist));
            } catch (Throwable e) {
                Logger.getLogger(MacOSPreferencesUtil.class.getName()).log(Level.WARNING, "Unexpected Exception " + e.getMessage(), e);

            }
        }
    }
}
