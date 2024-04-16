/*
 * @(#)ResourcesHelper.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.resources;

import javafx.scene.Node;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.fxbase.spi.NodeReader;
import org.jhotdraw8.fxbase.spi.NodeReaderRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

class ResourcesHelper {
    static final @NonNull Logger LOG = Logger.getLogger(Resources.class.getName());
    /**
     * The global map of property name modifiers. The key of this map is the
     * name of the property name modifier, the value of this map is a fallback
     * chain.
     */
    final static @NonNull Map<String, String[]> propertyNameModifiers = Collections.synchronizedMap(new HashMap<>());


    static {
        String osName = System.getProperty("os.name").toLowerCase();
        String os;
        if (osName.startsWith("mac")) {
            os = "mac";
        } else if (osName.startsWith("windows")) {
            os = "win";
        } else {
            os = "other";
        }
        propertyNameModifiers.put("os", new String[]{os, "default"});
    }

    static final @NonNull Set<String> acceleratorKeys = Collections.synchronizedSet(new HashSet<>(
            Arrays.asList("shift", "control", "ctrl", "meta", "alt", "altGraph")));
    /**
     * List of decoders. The first decoder which can decode a resource value is
     * will be used to convert the resource value to an object.
     */
    final static @NonNull List<ResourceDecoder> decoders = Collections.synchronizedList(new ArrayList<>());

    /**
     * Generates fallback keys by processing all property name modifiers in the
     * key.
     */
    static void generateFallbackKeys(@NonNull String key, @NonNull ArrayList<String> fallbackKeys) {
        int p1 = key.indexOf("[$");
        if (p1 < 0) {
            fallbackKeys.add(key);
        } else {
            int p2 = key.indexOf(']', p1 + 2);
            if (p2 < 0) {
                return;
            }
            String modifierKey = key.substring(p1 + 2, p2);
            String[] modifierValues = ResourcesHelper.propertyNameModifiers.get(modifierKey);
            if (modifierValues == null) {
                modifierValues = new String[]{"default"};
            }
            for (String mv : modifierValues) {
                generateFallbackKeys(key.substring(0, p1) + mv + key.substring(p2 + 1), fallbackKeys);
            }
        }
    }

    static @Nullable Node getIconProperty(@NonNull Resources r, String key, String suffix, @NonNull Class<?> baseClass) {
        try {
            String rsrcName = r.getString(key + suffix);
            if (rsrcName.isEmpty()) {
                return null;
            }

            for (ResourceDecoder d : ResourcesHelper.decoders) {
                if (d.canDecodeValue(key, rsrcName, Node.class)) {
                    return d.decode(key, rsrcName, Node.class, baseClass);
                }
            }


            if (r.getModule() != null) {
                try {
                    Object module = r.getModule();
                    NodeReader reader = NodeReaderRegistry.getNodeReader(rsrcName);
                    if (reader != null) {
                        try (InputStream resourceAsStream = (InputStream) module.getClass().getMethod("getResourceAsStream", String.class)
                                .invoke(module, rsrcName)) {
                            if (resourceAsStream != null) {
                                return reader.read(resourceAsStream);
                            }
                        }
                    }
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    // we retry with baseClass
                }
            }

            URL url = baseClass.getResource(rsrcName);
            if (url == null) {
                ResourcesHelper.LOG.warning("Resources[" + r.getBaseName() + "].getIconProperty \"" + key + suffix + "\" resource:" + rsrcName + " not found.");
                return null;
            }
            NodeReader reader = NodeReaderRegistry.getNodeReader(url);
            return reader == null ? null : reader.read(url);

        } catch (MissingResourceException | IOException e) {
            ResourcesHelper.LOG.log(Level.WARNING, "Resources[" + r.getBaseName() + "].getIconProperty \"" + key + suffix + "\" not found.", e);
            return null;
        }
    }

}
