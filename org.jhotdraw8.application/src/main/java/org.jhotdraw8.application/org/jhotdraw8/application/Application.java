/*
 * @(#)Application.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlySetProperty;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.scene.Node;
import javafx.scene.control.MenuBar;
import javafx.scene.input.DataFormat;
import org.jhotdraw8.application.action.Action;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.fxbase.beans.NonNullObjectProperty;
import org.jhotdraw8.fxbase.beans.PropertyBean;
import org.jhotdraw8.fxbase.concurrent.FXWorker;
import org.jhotdraw8.fxbase.control.Disableable;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.SimpleNullableKey;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.prefs.Preferences;

/**
 * An {@code Application} handles the life-cycle of {@link Activity} objects and
 * provides windows to present them on screen.
 *
 */
public interface Application extends Disableable, PropertyBean {
    String ACTIONS_PROPERTY = "actions";
    String ACTIVITIES_PROPERTY = "activities";
    String ACTIVITY_FACTORY_PROPERTY = "activityFactory";
    String RESOURCE_BUNDLE_PROPERTY = "resourceBundle";
    String MENU_BAR_FACTORY_PROPERTY = "menuBarFactory";
    String RECENT_URIS_PROPERTY = "recentUris";
    String PREFERENCES_PROPERTY = "preferences";
    String MAX_NUMBER_OF_RECENT_URIS_PROPERTY = "maxNumberOfRecentUris";
    String STYLESHEETS_PROPERTY = "stylesheets";

    Key<String> NAME_KEY = new SimpleNullableKey<>("name", String.class);
    Key<String> VERSION_KEY = new SimpleNullableKey<>("version", String.class);
    Key<String> COPYRIGHT_KEY = new SimpleNullableKey<>("copyright", String.class);
    Key<String> LICENSE_KEY = new SimpleNullableKey<>("license", String.class);

    /**
     * Contains all {@link Activity} objects that are managed by this
     * {@link Application}.
     *
     * @return the activities
     */
    ReadOnlySetProperty<Activity> activitiesProperty();

    Executor getExecutor();

    NonNullObjectProperty<Preferences> preferencesProperty();


    /**
     * Contains all {@link Action} objects that are managed by this
     * {@link Application}.
     *
     * @return the activities
     */
    ReadOnlyMapProperty<String, Action> actionsProperty();


    default ObservableMap<String, Action> getActions() {
        return actionsProperty().get();
    }

    default Preferences getPreferences() {
        return preferencesProperty().get();
    }

    default void setPreferences(Preferences preferences) {
        preferencesProperty().set(preferences);
    }

    /**
     * The set of recent URIs. The set must be ordered by most recently used
     * first. Only the first items as specified in
     * {@link #maxNumberOfRecentUrisProperty} of the set are used and persisted
     * in user preferences.
     *
     * @return the recent Uris
     */
    ReadOnlyMapProperty<URI, DataFormat> recentUrisProperty();

    default ObservableMap<URI, DataFormat> getRecentUris() {
        return recentUrisProperty().get();
    }

    /**
     * The maximal number of recent URIs. Specifies how many items of
     * {@link #recentUrisProperty} are used and persisted in user preferences.
     * This number is also persisted in user preferences.
     *
     * @return the number of recent Uris
     */
    IntegerProperty maxNumberOfRecentUrisProperty();

    // Convenience method
    default ObservableSet<Activity> getActivities() {
        return activitiesProperty().get();
    }

    /**
     * Provides the currently active activities. This is the last activities which was
     * focus owner. Returns null, if the application has no views.
     *
     * @return The active activities.
     */
    ReadOnlyObjectProperty<Activity> activeActivityProperty();

    // Convenience method
    default @Nullable Activity getActiveActivity() {
        return activeActivityProperty().get();
    }


    /**
     * Exits the application.
     */
    void exit();

    /**
     * Returns the application node.
     *
     * @return the node
     */
    default @Nullable Node getNode() {
        return null;
    }

    /**
     * Creates a new activity, initializes it, then invokes the callback.
     *
     * @return A callback.
     */
    default CompletionStage<Activity> createActivity() {
        return FXWorker.supply(() -> {
            Supplier<Activity> factory = getActivityFactory();
            if (factory == null) {
                throw new IllegalStateException("Could not create a new Activity, because no activityFactory has been set.");
            }
            return factory.get();
        });
    }

    default int getMaxNumberOfRecentUris() {
        return maxNumberOfRecentUrisProperty().get();
    }

    default void setMaxNumberOfRecentUris(int newValue) {
        maxNumberOfRecentUrisProperty().set(newValue);
    }

    ObjectProperty<Supplier<Activity>> activityFactoryProperty();

    default Supplier<Activity> getActivityFactory() {
        return activityFactoryProperty().get();
    }

    default void setActivityFactory(Supplier<Activity> newValue) {
        activityFactoryProperty().set(newValue);
    }

    ObjectProperty<Supplier<MenuBar>> menuBarFactoryProperty();

    NonNullObjectProperty<Resources> resourcesProperty();

    default @Nullable Supplier<MenuBar> getMenuBarFactory() {
        return menuBarFactoryProperty().get();
    }

    ReadOnlyListProperty<String> stylesheetsProperty();

    default ObservableList<String> getStylesheets() {
        return stylesheetsProperty().get();
    }

    default void setMenuBarFactory(@Nullable Supplier<MenuBar> newValue) {
        menuBarFactoryProperty().set(newValue);
    }

    default Resources getResources() {
        return resourcesProperty().get();
    }

    default void setResources(Resources newValue) {
        resourcesProperty().set(newValue);
    }
}
