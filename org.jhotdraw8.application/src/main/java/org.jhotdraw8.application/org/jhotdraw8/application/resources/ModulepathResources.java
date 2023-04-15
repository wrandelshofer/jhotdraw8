/*
 * @(#)ModulepathResources.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.resources;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Logger;
import java.util.spi.ResourceBundleProvider;

public class ModulepathResources extends ResourceBundle implements Serializable, Resources {
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(ModulepathResources.class.getName());

    /**
     * Get the appropriate ResourceBundle subclass.
     *
     * @param baseName the base name
     * @return the resource bundle
     * @see ResourceBundle
     */
    public static @NonNull ModulepathResources getResources(@NonNull String moduleName, @NonNull String baseName)
            throws MissingResourceException {
        return getResources(moduleName, baseName, LocaleUtil.getDefault());
    }

    /**
     * Get the appropriate ResourceBundle subclass.
     *
     * @param baseName the base name
     * @return the resource bundle
     * @see ResourceBundle
     */
    public static @NonNull Resources getResources(@NonNull Module module, @NonNull String baseName)
            throws MissingResourceException {
        return getResources(module, baseName, LocaleUtil.getDefault());
    }

    /**
     * Get the appropriate ResourceBundle subclass.
     *
     * @param baseName the base name
     * @param locale   the locale
     * @return the resource bundle
     * @see ResourceBundle
     */
    static ModulepathResources getResources(@NonNull String moduleName, @NonNull String baseName, @NonNull Locale locale)
            throws MissingResourceException {
        ModulepathResources r;
        r = new ModulepathResources(ModuleLayer.boot().findModule(moduleName).orElseThrow(
                () -> new MissingResourceException("Can't find module " + moduleName, baseName, locale.toString())
        ), baseName, locale);
        return r;
    }

    /**
     * Get the appropriate ResourceBundle subclass.
     *
     * @param baseName the base name
     * @param locale   the locale
     * @return the resource bundle
     * @see ResourceBundle
     */
    public static ModulepathResources getResources(@NonNull Module module, @NonNull String baseName, @NonNull Locale locale)
            throws MissingResourceException {
        ModulepathResources r;
        r = new ModulepathResources(module, baseName, locale);
        return r;
    }

    /**
     * The base class
     */
    private Class<?> baseClass = getClass();
    /**
     * The base name of the resource bundle.
     */
    private final @NonNull String baseName;
    /**
     * The locale.
     */
    private final @NonNull Locale locale;

    /**
     * The parent resources object.
     */
    private @Nullable Resources parent;
    /**
     * The wrapped resource bundle.
     */
    private final transient ResourceBundle resource;
    /**
     * The module from which the resource bundle was instantiated
     * and which we use to load resources (i.e. images) from.
     */
    private final Module module;

    /**
     * Creates a new object which wraps the provided resource bundle.
     *
     * @param baseName the base name
     * @param locale   the locale
     */
    public ModulepathResources(@NonNull Module module, @NonNull String baseName, @NonNull Locale locale) throws MissingResourceException {
        this.locale = locale;
        this.baseName = baseName;
        this.resource = doGetBundle(module, baseName, locale);
        this.module = module;

        ModulepathResources potentialParent = null;
        String moduleAndParentBaseName = null;
        try {
            moduleAndParentBaseName = this.resource.getString(PARENT_RESOURCE_KEY);
        } catch (MissingResourceException e) {

        }
        if (moduleAndParentBaseName != null) {
            String[] split = moduleAndParentBaseName.split("\\s+|\\s*,\\s*");
            String parentBaseName;
            Module parentModule;
            switch (split.length) {
            case 1:
                parentModule = module;
                parentBaseName = split[0];
                break;
            case 2:
                parentModule = ModuleLayer.boot().findModule(split[0]).orElseThrow(
                        () -> new MissingResourceException("Can't find module " + split[0], baseName, locale.toString()));
                parentBaseName = split[1];
                break;
            default:
                throw new IllegalArgumentException("Illegal " + PARENT_RESOURCE_KEY + " resource " + moduleAndParentBaseName);
            }
            try {
                potentialParent = new ModulepathResources(parentModule, parentBaseName, locale);
            } catch (MissingResourceException e) {
                MissingResourceException ex = new MissingResourceException(
                        "Can't find parent bundle $parent=\"" + moduleAndParentBaseName +
                                "\" specified in " + module + "," + baseName + "," + locale, baseName, "");
                ex.initCause(e);
                throw ex;
            }
        }
        this.parent = potentialParent;
    }

    private static ResourceBundle doGetBundle(Module module, String baseName, @NonNull Locale locale) {
        for (ResourceBundleProvider provider : ServiceLoader.load(ResourceBundleProvider.class)) {
            if (provider.getClass().getModule().equals(module)) {
                ResourceBundle bundle = provider.getBundle(baseName, locale);
                if (bundle != null) {
                    return bundle;
                }
            }
        }
        throw new MissingResourceException("Can't find bundle " + module + "," + baseName + "," + locale, baseName, locale.toString());
    }


    @Override
    public @NonNull ResourceBundle asResourceBundle() {
        return this;
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
        return false;
    }


    @Override
    public Class<?> getBaseClass() {
        return baseClass;
    }

    @Override
    public @NonNull String getBaseName() {
        return baseName;
    }

    public void setBaseClass(Class<?> baseClass) {
        this.baseClass = baseClass;
    }

    /**
     * Returns a formatted string using javax.text.MessageFormat.
     *
     * @param key       the key
     * @param arguments the arguments
     * @return formatted String
     */
    @Override
    public @NonNull String getFormatted(@NonNull String key, Object... arguments) {
        return MessageFormat.format(getString(key), arguments);
    }

    @Override
    protected @Nullable Object handleGetObject(@NonNull String key) {
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
    public @Nullable Object handleGetObjectRecursively(@NonNull String key) {
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
    public @NonNull String toString() {
        return "ModulepathResources" + "[" + baseName + "]";
    }

    @Override
    public Module getModule() {
        return module;
    }

    @Override
    public @NonNull Enumeration<String> getKeys() {
        Set<String> keys = new LinkedHashSet<>();

        for (String key : (Iterable<String>) () -> resource.getKeys().asIterator()) {
            keys.add(key);
        }
        if (parent != null) {
            for (String key : (Iterable<String>) () -> parent.getKeys().asIterator()) {
                keys.add(key);
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
