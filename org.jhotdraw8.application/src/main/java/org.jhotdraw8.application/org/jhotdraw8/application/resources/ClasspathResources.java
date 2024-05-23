/*
 * @(#)ClasspathResources.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.resources;

import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.SequencedSet;
import java.util.logging.Logger;


@SuppressWarnings({"serial", "RedundantSuppression"})
public class ClasspathResources extends ResourceBundle implements Serializable, Resources {
    private static final Logger LOG = Logger.getLogger(ClasspathResources.class.getName());
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The base class
     */
    private final Class<?> baseClass = getClass();
    /**
     * The base name of the resource bundle.
     */
    private final String baseName;
    /**
     * The locale. This field is currently only used for debugging.
     */
    private final Locale locale;

    /**
     * The parent resources object.
     */
    private @Nullable Resources parent;

    /**
     * The wrapped resource bundle.
     */
    private final transient ResourceBundle resource;

    /**
     * Creates a new ClasspathResources object which wraps the provided resource bundle.
     *
     * @param baseName the base name
     * @param locale   the locale
     */
    public ClasspathResources(String baseName, Locale locale) {
        this.locale = locale;
        this.baseName = baseName;
        this.resource = ResourceBundle.getBundle(baseName, locale);

        ClasspathResources potentialParent = null;
        String moduleAndParentBaseName = null;
        try {
            moduleAndParentBaseName = this.resource.getString(PARENT_RESOURCE_KEY);
        } catch (MissingResourceException e) {

        }
        if (moduleAndParentBaseName != null) {
            String[] split = moduleAndParentBaseName.split("\\s+|\\s*,\\s*");
            String parentBaseName;
            String moduleName;
            parentBaseName = switch (split.length) {
                case 1 -> {
                    moduleName = "";
                    yield split[0];
                }
                case 2 -> {
                    moduleName = split[0];
                    yield split[1];
                }
                default ->
                        throw new IllegalArgumentException("Could not parse the value of the property " + PARENT_RESOURCE_KEY + "=\"" + moduleAndParentBaseName + "\".");
            };
            try {
                potentialParent = new ClasspathResources(parentBaseName, locale);
            } catch (MissingResourceException e) {
                MissingResourceException ex = new MissingResourceException("Could not find a resource bundle with baseName=\"" + baseName + " and locale=\"" + locale + "\".", baseName, locale.toString());
                ex.initCause(e);
                throw ex;
            }
        }
        this.parent = potentialParent;
    }

    @Override
    public boolean containsKey(@Nullable String key) {
        Objects.requireNonNull(key, "key");
        if (resource.containsKey(key)) {
            return true;
        }
        if (parent != null) {
            return parent.containsKey(key);
        }
        LOG.warning("Could not find a resource with key=\"" + key + "\" in resource bundle \"" + baseName + "\".");
        return false;
    }


    @Override
    public Class<?> getBaseClass() {
        return baseClass;
    }

    @Override
    public @Nullable Object getModule() {
        return null;
    }


    @Override
    protected @Nullable Object handleGetObject(String key) {
        Object obj = handleGetObjectRecursively(key);
        if (obj == null) {
            obj = "";
            LOG.warning("Can't find resource for bundle " + baseName + ", key " + key);
        }

        if (obj instanceof String) {
            obj = substitutePlaceholders(key, (String) obj);
        }
        return obj;
    }

    @Override
    public @Nullable Object handleGetObjectRecursively(String key) {
        Object obj = null;
        try {
            obj = resource.getObject(key);
        } catch (MissingResourceException e) {
            if (parent != null) {
                return parent.handleGetObjectRecursively(key);
            }
        }
        return obj;
    }

    @Override
    public String toString() {
        return "ClasspathResources" + "[" + baseName + "]";
    }


    @Override
    public String getBaseName() {
        return baseName;
    }

    /**
     * Get the appropriate ResourceBundle subclass.
     *
     * @param baseName the base name
     * @return the resource bundle
     * @see ResourceBundle
     */
    public static Resources getResources(String baseName)
            throws MissingResourceException {
        return getResources(baseName, LocaleUtil.getDefault());
    }

    /**
     * Get the appropriate ResourceBundle subclass.
     *
     * @param baseName the base name
     * @param locale   the locale
     * @return the resource bundle
     * @see ResourceBundle
     */
    static Resources getResources(String baseName, Locale locale)
            throws MissingResourceException {
        Resources r;
        r = new ClasspathResources(baseName, locale);
        return r;
    }

    @Override
    public ResourceBundle asResourceBundle() {
        return this;
    }

    @Override
    public Locale getLocale() {
        return super.getLocale();
    }

    @Override
    public Enumeration<String> getKeys() {
        SequencedSet<String> keys = new LinkedHashSet<>();

        for (Enumeration<String> i = resource.getKeys(); i.hasMoreElements(); ) {
            keys.add(i.nextElement());
        }
        if (parent != null) {
            for (Enumeration<String> i = parent.getKeys(); i.hasMoreElements(); ) {
                keys.add(i.nextElement());
            }
        }

        return Collections.enumeration(keys);
    }

    @Override
    public @Nullable Resources getParent() {
        return parent;
    }

    @Override
    public void setParent(@Nullable Resources parent) {
        this.parent = parent;
    }
}
