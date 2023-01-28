/*
 * @(#)MacOSPreferencesUtil.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.os.macos;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.StreamSupport;

/**
 * Provides read methods for some well known macOS preferences files.
 */
public class MacOSPreferencesUtil {
    /**
     * Path to global preferences.
     */
    public static final File GLOBAL_PREFERENCES = new File(System.getProperty("user.home"), "Library/Preferences/.GlobalPreferences.plist");
    /**
     * Path to finder preferences.
     */
    public static final File FINDER_PREFERENCES = new File(System.getProperty("user.home"), "Library/Preferences/com.apple.finder.plist");
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
        if (plist instanceof List) {
            for (Object o : (List) plist) {
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
        if (plist instanceof List) {
            for (Object o : (List) plist) {
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
            Map<String, Object> cache = new HashMap<String, Object>();
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

    private static boolean isMacOs() {
        final String osName = System.getProperty("os.name");
        return osName != null && osName.toLowerCase().startsWith("mac");
    }

    public static void readPreferences(@NonNull File file, @NonNull Map<String, Object> cache) {
        cache.clear();

        if (isMacOs()) {
            try {
                Document plist = readPList(file);
                cache.put("plist", readNode(plist.getDocumentElement()));
            } catch (Throwable e) {
                System.err.println("Warning: ch.randelshofer.quaqua.util.OSXPreferences failed to load " + file);
                e.printStackTrace();
            }
        }
    }

    private static Object readNode(@NonNull Element node) throws IOException {
        String name = node.getTagName();
        Object value;
        switch (name) {
            case "plist":
                value = readPList(node);
                break;
            case "dict":
                value = readDict(node);
                break;
            case "array":
                value = readArray(node);
                break;
            default:
                value = readValue(node);
                break;
        }
        return value;
    }

    private static @NonNull Iterable<Node> getChildren(final @NonNull Element elem) {
        return () -> new Iterator<Node>() {
            int index = 0;
            final NodeList children = elem.getChildNodes();

            @Override
            public boolean hasNext() {
                return index < children.getLength();
            }

            @Override
            public Node next() {
                return children.item(index++);
            }
        };
    }

    private static @NonNull Iterable<Element> getChildElements(final @NonNull Element elem) {
        return () -> StreamSupport.stream(getChildren(elem).spliterator(), false)
                .filter(e -> e instanceof Element).map(e -> (Element) e).iterator();
    }

    private static @NonNull List<Object> readPList(@NonNull Element plistElem) throws IOException {
        List<Object> plist = new ArrayList<>();
        for (Node child : getChildElements(plistElem)) {
            plist.add(readNode((Element) child));
        }
        return plist;
    }

    private static @NonNull String getContent(@NonNull Element elem) {
        StringBuilder buf = new StringBuilder();
        for (Node child : getChildren(elem)) {
            if (child instanceof Text) {
                buf.append(child.getTextContent());
            }
        }
        return buf.toString().trim();
    }

    private static @NonNull Map<String, Object> readDict(@NonNull Element dictElem) throws IOException {
        LinkedHashMap<String, Object> dict = new LinkedHashMap<>();
        for (Iterator<Element> iterator = getChildElements(dictElem).iterator(); iterator.hasNext(); ) {
            Element keyElem = iterator.next();
            if (!"key".equals(keyElem.getTagName())) {
                throw new IOException("missing dictionary key at" + dictElem);
            }
            Element valueElem = iterator.next();
            Object elemValue = readNode(valueElem);
            dict.put(getContent(keyElem), elemValue);
        }
        return dict;
    }

    private static @NonNull List<Object> readArray(@NonNull Element arrayElem) throws IOException {
        List<Object> array = new ArrayList<>();
        for (Element child : getChildElements(arrayElem)) {
            array.add(readNode(child));
        }
        return array;
    }

    private static Object readValue(@NonNull Element value) throws IOException {
        Object parsedValue;
        switch (value.getTagName()) {
            case "true":
                parsedValue = true;
                break;
            case "false":
                parsedValue = false;
                break;
            case "data":
                parsedValue = Base64.getDecoder().decode(getContent(value));
                break;
            case "date":
                try {
                    parsedValue = DatatypeFactory.newInstance().newXMLGregorianCalendar(getContent(value));
                } catch (IllegalArgumentException |
                         DatatypeConfigurationException e) {
                    throw new IOException(e);
                }
                break;
            case "real":
                try {
                    parsedValue = Double.valueOf(getContent(value));
                } catch (NumberFormatException e) {
                    throw new IOException(e);
                }
                break;
            case "integer":
                try {
                    parsedValue = Long.valueOf(getContent(value));
                } catch (NumberFormatException e) {
                    throw new IOException(e);
                }
                break;
            default:
                parsedValue = getContent(value);
                break;
        }
        return parsedValue;
    }

    private static Document readXmlPropertyList(@NonNull File file) throws IOException {
        InputSource inputSource = new InputSource(file.toString());
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        DocumentBuilder builder;
        try {
            builder = builderFactory.newDocumentBuilder();
            // We do not want that the reader creates a socket connection!
            builder.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));
            return builder.parse(inputSource);
        } catch (ParserConfigurationException e) {
            throw new IOException("Cannot create document builder for file: " + file, e);
        } catch (SAXException e) {
            throw new IOException("Illegal file format in file: " + file, e);
        }
    }

    private static Document readBinaryPropertyList(File file) throws IOException {
        return new BinaryPListParser().parse(file);
    }

    /**
     * Reads the specified PList file and returns it as a document.
     * This method can deal with XML encoded and binary encoded PList files.
     */
    private static Document readPList(@NonNull File file) throws IOException {
        Document doc;
        try {
            doc = readBinaryPropertyList(file);
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            try {
                doc = readXmlPropertyList(file);
            } catch (IOException e3) {
                throw e3;
            }
        }
        if (doc == null) {
            throw new IOException("File is neither an XML PList nor a Binary PList. File: " + file);
        }
        return doc;
    }
}
