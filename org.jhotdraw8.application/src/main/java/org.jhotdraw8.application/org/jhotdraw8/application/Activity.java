/*
 * @(#)Activity.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import org.jhotdraw8.application.action.Action;
import org.jhotdraw8.fxbase.beans.PropertyBean;
import org.jhotdraw8.fxbase.control.Disableable;
import org.jspecify.annotations.Nullable;

/// Represents an activity that the user performs with an [Application].
///
/// The life-cycle of an [Activity] is managed by an [Application],
/// it consists of the following steps:
/// <ol>
///   - **Creation**
///     The user invokes an `Action` that instantiates a new [Activity]
///     for the [Application], which is passed as an argument to the constructor
///     of the [Activity].
///
///   - **Initialisation**
///     The `Action` adds the [Activity]  to the [Application].
///     The [Application] invokes [#init].
///     The [Application] retrieves [Action]s from the [Activity]
///     and creates user interface elements for them.
///     The [Application] retrieves the [Node] from the [Activity]
///     and adds it to one of its scene graphs.
///
///   - **Start**
///     The [Application] invokes [#start()], to
///     inform the activity that it can start (or resume) execution.
///
///   - **Stop**
///     The [Application] invokes [#stop()], to
///     inform the activity that it should stop (or suspend) execution.
///     The [Application] can invoke [#start()] again, to
///     resume execution.
///
///   - **Destruction**
///     When the view is no longer needed, the [Application] ensures that the
///     activity is destroyed.
///     The [Application] invokes [#destroy].
///     The [Application] removes the `Node` of the [Activity]
///     from its scene graph.
///     The [Application] sets the [#applicationProperty()] to null.
///
/// </ol>
///
/// An activity can be disabled. See [Disableable].
///
/// [Action]s can store arbitrary transient data in the activity.
/// This facility is provided by extending the interface [PropertyBean].
public interface Activity extends Disableable, PropertyBean {
    String APPLICATION_PROPERTY = "application";
    String DISAMBIGUATION_PROPERTY = "disambiguation";
    String TITLE_PROPERTY = "title";

    /// Contains all [Action] objects that are managed by this
    /// [Activity].
    ///
    /// @return the activities
    ReadOnlyMapProperty<String, Action> actionsProperty();

    default ObservableMap<String, Action> getActions() {
        return actionsProperty().get();
    }

    /// The application property is maintained by the [Application]
    /// that manages this activity.
    ///
    /// The value is set to the application before [#init] is called.
    ///
    /// The value is set to null after [#destroy] has been called.
    ///
    /// @return the property
    ObjectProperty<Application> applicationProperty();

    /// Used by the application to display unique titles if multiple
    /// activities have the same title.
    ///
    /// @return the property
    IntegerProperty disambiguationProperty();


    // getter and setter methods for properties
    default void setApplication(Application application) {
        applicationProperty().set(application);
    }

    default Application getApplication() {
        Application application = applicationProperty().get();
        if (application == null) {
            throw new NullPointerException(
                    "application was not initialized with a non-null value in the constructor of an Activity.");
        }
        return application;
    }


    default int getDisambiguation() {
        return disambiguationProperty().get();
    }

    default void setDisambiguation(int newValue) {
        disambiguationProperty().set(newValue);
    }

    /// Returns a JavaFX node that provides a user interface for the activity.
    ///
    /// @return the node
    Node getNode();

    default @Nullable String getTitle() {
        return titleProperty().get();
    }

    default void setTitle(@Nullable String newValue) {
        titleProperty().set(newValue);
    }

    /// Initializes the activity.
    ///
    /// See life-cycle in [Activity].
    void init();

    /// Starts the activity.
    ///
    /// See life-cycle in [Activity].
    void start();

    /// Stops the activity.
    ///
    /// See life-cycle in [Activity].
    void stop();

    /// Destroys the activity.
    ///
    /// See life-cycle in [Activity].
    void destroy();

    /// The title of the activity as displayed in the title bars of the
    /// activity’s windows and in alert dialogs related to the activity.
    ///
    /// If the activity can be associated to a name, then the title
    /// should be that name. If the name has not been assigned yet, the
    /// title should be 'unnamed'.
    ///
    /// See [FileBasedActivity#titleProperty()].
    ///
    /// @return the title of the activity
    StringProperty titleProperty();

}
