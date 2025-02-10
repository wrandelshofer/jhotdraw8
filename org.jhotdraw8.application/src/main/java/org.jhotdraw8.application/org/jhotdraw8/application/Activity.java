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

/**
 * Represents an activity that the user performs with an {@link Application}.
 * <p>
 * The life-cycle of an {@link Activity} is managed by an {@link Application},
 * it consists of the following steps:
 * <ol>
 * <li><b>Creation</b><br>
 * The user invokes an {@code Action} that instantiates a new {@link Activity}
 * for the {@link Application}, which is passed as an argument to the constructor
 * of the {@link Activity}.
 * </li>
 * <li><b>Initialisation</b><br>
 * The {@code Action} adds the {@link Activity}  to the {@link Application}.<br>
 * The {@link Application} invokes {@link #init}.<br>
 * The {@link Application} retrieves {@link Action}s from the {@link Activity}
 * and creates user interface elements for them.
 * The {@link Application} retrieves the {@link Node} from the {@link Activity}
 * and adds it to one of its scene graphs.
 * </li>
 * <li><b>Start</b><br>
 * The {@link Application} invokes {@link #start()}, to
 * inform the activity that it can start (or resume) execution.
 * </li>
 * <li><b>Stop</b><br>
 * The {@link Application} invokes {@link #stop()}, to
 * inform the activity that it should stop (or suspend) execution.<br>
 * The {@link Application} can invoke {@link #start()} again, to
 * resume execution.
 * </li>
 * <li><b>Destruction</b><br>
 * When the view is no longer needed, the {@link Application} ensures that the
 * activity is destroyed.<br>
 * The {@link Application} invokes {@link #destroy}.<br>
 * The {@link Application} removes the {@code Node} of the {@link Activity}
 * from its scene graph.<br>
 * The {@link Application} sets the {@link #applicationProperty()} to null.<br>
 * </li>
 * </ol>
 * <p>
 * An activity can be disabled. See {@link Disableable}.
 * <p>
 * {@link Action}s can store arbitrary transient data in the activity.
 * This facility is provided by extending the interface {@link PropertyBean}.
 *
 */
public interface Activity extends Disableable, PropertyBean {
    String APPLICATION_PROPERTY = "application";
    String DISAMBIGUATION_PROPERTY = "disambiguation";
    String TITLE_PROPERTY = "title";

    /**
     * Contains all {@link Action} objects that are managed by this
     * {@link Activity}.
     *
     * @return the activities
     */
    ReadOnlyMapProperty<String, Action> actionsProperty();

    default ObservableMap<String, Action> getActions() {
        return actionsProperty().get();
    }

    /**
     * The application property is maintained by the {@link Application}
     * that manages this activity.
     * <p>
     * The value is set to the application before {@link #init} is called.
     * <p>
     * The value is set to null after {@link #destroy} has been called.
     *
     * @return the property
     */
    ObjectProperty<Application> applicationProperty();

    /**
     * Used by the application to display unique titles if multiple
     * activities have the same title.
     *
     * @return the property
     */
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

    /**
     * Returns a JavaFX node that provides a user interface for the activity.
     *
     * @return the node
     */
    Node getNode();

    default @Nullable String getTitle() {
        return titleProperty().get();
    }

    default void setTitle(@Nullable String newValue) {
        titleProperty().set(newValue);
    }

    /**
     * Initializes the activity.
     * <p>
     * See life-cycle in {@link Activity}.
     */
    void init();

    /**
     * Starts the activity.
     * <p>
     * See life-cycle in {@link Activity}.
     */
    void start();

    /**
     * Stops the activity.
     * <p>
     * See life-cycle in {@link Activity}.
     */
    void stop();

    /**
     * Destroys the activity.
     * <p>
     * See life-cycle in {@link Activity}.
     */
    void destroy();

    /**
     * The title of the activity as displayed in the title bars of the
     * activity’s windows and in alert dialogs related to the activity.
     * <p>
     * If the activity can be associated to a name, then the title
     * should be that name. If the name has not been assigned yet, the
     * title should be 'unnamed'.
     * <p>
     * See {@link FileBasedActivity#titleProperty()}.
     *
     * @return the title of the activity
     */
    StringProperty titleProperty();

}
