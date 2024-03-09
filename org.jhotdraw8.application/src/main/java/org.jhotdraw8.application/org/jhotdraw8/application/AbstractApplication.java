/*
 * @(#)AbstractApplication.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.ReadOnlyMapWrapper;
import javafx.beans.property.ReadOnlySetProperty;
import javafx.beans.property.ReadOnlySetWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.scene.control.MenuBar;
import javafx.scene.input.DataFormat;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.application.action.Action;
import org.jhotdraw8.application.resources.EmptyResources;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.fxbase.beans.NonNullObjectProperty;
import org.jhotdraw8.fxcollection.typesafekey.Key;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * AbstractApplication.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractApplication extends javafx.application.Application implements org.jhotdraw8.application.Application {

    private static final @NonNull String RECENT_URIS = ".recentUriFormats";

    /**
     * Holds the disabled state.
     */
    private final @NonNull ReadOnlyBooleanProperty disabled;
    /**
     * Holds the disablers.
     */
    private final @NonNull ObservableSet<Object> disablers = FXCollections.observableSet();

    private final @NonNull ReadOnlyMapProperty<String, Action> actions = new ReadOnlyMapWrapper<String, Action>(this, ACTIONS_PROPERTY, FXCollections.observableMap(new LinkedHashMap<>())).getReadOnlyProperty();
    private final @NonNull ReadOnlySetProperty<Activity> activities = new ReadOnlySetWrapper<Activity>(this, ACTIVITIES_PROPERTY, FXCollections.observableSet(new LinkedHashSet<>())).getReadOnlyProperty();
    private final @NonNull ReadOnlyListProperty<String> stylesheets = new ReadOnlyListWrapper<String>(this, STYLESHEETS_PROPERTY, FXCollections.observableArrayList()).getReadOnlyProperty();

    private final @NonNull ObjectProperty<Supplier<Activity>> activityFactory = new SimpleObjectProperty<>(this, ACTIVITY_FACTORY_PROPERTY);
    private final @NonNull ObjectProperty<Supplier<MenuBar>> menuFactory = new SimpleObjectProperty<>(this, MENU_BAR_FACTORY_PROPERTY);
    private final @NonNull NonNullObjectProperty<Resources> resources = new NonNullObjectProperty<>(this, RESOURCE_BUNDLE_PROPERTY, new EmptyResources());
    private final @NonNull NonNullObjectProperty<Preferences> preferences = new NonNullObjectProperty<>(this, PREFERENCES_PROPERTY, Preferences.userNodeForPackage(getClass()));

    /**
     * Holds the max number of recent URIs.
     */
    private final IntegerProperty maxNumberOfRecentUris//
            = new SimpleIntegerProperty(//
            this, MAX_NUMBER_OF_RECENT_URIS_PROPERTY, //
            10);
    /**
     * Properties.
     */
    private ObservableMap<Key<?>, Object> properties;

    /**
     * Holds the recent URIs.
     */
    private final ReadOnlyMapProperty<URI, DataFormat> recentUris//
            = new ReadOnlyMapWrapper<URI, DataFormat>(//
            this, RECENT_URIS_PROPERTY, //
            FXCollections.observableMap(new LinkedHashMap<>(16, 0.5f, true))).getReadOnlyProperty();

    {// initializer for 'disabled' property
        ReadOnlyBooleanWrapper robw = new ReadOnlyBooleanWrapper(this, DISABLED_PROPERTY);
        robw.bind(Bindings.isNotEmpty(disablers));
        disabled = robw.getReadOnlyProperty();
    }

    public AbstractApplication() {
    }

    @Override
    public ReadOnlyBooleanProperty disabledProperty() {
        return disabled;
    }

    @Override
    public @NonNull ObservableSet<Object> disablers() {
        return disablers;
    }

    @Override
    public final @NonNull ObservableMap<Key<?>, Object> getProperties() {
        if (properties == null) {
            properties = FXCollections.observableHashMap();
        }
        return properties;
    }

    protected void loadRecentUris(String applicationId) {
        Preferences prefs = getPreferences();
        String recentUrisSerialized = prefs.get(applicationId + RECENT_URIS, "");
        for (String row : recentUrisSerialized.split("\n")) {
            if (row.isEmpty()) {
                continue;
            }
            if (recentUris.size() >= getMaxNumberOfRecentUris()) {
                break;
            }
            String[] columns = row.split("\t");
            if (columns.length < 1) {
                continue;
            }
            try {
                URI uri = new URI(columns[0]);
                DataFormat format = null;
                if (columns.length > 1 && !columns[1].isEmpty()) {
                    format = DataFormat.lookupMimeType(columns[1]);
                    if (format == null) {
                        format = new DataFormat(columns[1]);
                    }
                }
                recentUris.put(uri, format);
            } catch (URISyntaxException ex) {
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unexpected Exception.", ex);

            }
        }
        recentUris.get().addListener((MapChangeListener.Change<? extends URI, ? extends DataFormat> change) -> {
            StringBuilder buf = new StringBuilder();
            int skip = recentUris.size() - getMaxNumberOfRecentUris();
            for (Map.Entry<URI, DataFormat> entry : recentUris.entrySet()) {
                if (skip-- > 0) {
                    continue;
                }
                if (!buf.isEmpty()) {
                    buf.append('\n');
                }
                URI uri = entry.getKey();
                DataFormat format = entry.getValue();
                if (uri != null) {
                    buf.append(uri);
                    if (format != null && format.getIdentifiers() != null && !format.getIdentifiers().isEmpty()) {
                        buf.append('\t').append(format.getIdentifiers().iterator().next());
                    }
                }
            }
            prefs.put(applicationId + RECENT_URIS, buf.toString());
        });
    }

    @Override
    public @NonNull IntegerProperty maxNumberOfRecentUrisProperty() {
        return maxNumberOfRecentUris;
    }

    @Override
    public ReadOnlyMapProperty<URI, DataFormat> recentUrisProperty() {
        return recentUris;
    }

    @Override
    public @NonNull ReadOnlyMapProperty<String, Action> actionsProperty() {
        return actions;
    }

    @Override
    public @NonNull ReadOnlySetProperty<Activity> activitiesProperty() {
        return activities;
    }

    @Override
    public @NonNull ReadOnlyListProperty<String> stylesheetsProperty() {
        return stylesheets;
    }

    @Override
    public @NonNull ObjectProperty<Supplier<Activity>> activityFactoryProperty() {
        return activityFactory;
    }

    @Override
    public @NonNull ObjectProperty<Supplier<MenuBar>> menuBarFactoryProperty() {
        return menuFactory;
    }

    @Override
    public @NonNull NonNullObjectProperty<Resources> resourcesProperty() {
        return resources;
    }

    @Override
    public @NonNull NonNullObjectProperty<Preferences> preferencesProperty() {
        return preferences;
    }

    @Override
    public Executor getExecutor() {
        return ForkJoinPool.commonPool();
    }
}

