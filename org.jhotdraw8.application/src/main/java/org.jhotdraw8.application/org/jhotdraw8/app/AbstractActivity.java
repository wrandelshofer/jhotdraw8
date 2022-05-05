/*
 * @(#)AbstractActivity.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.app;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.ReadOnlyMapWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.app.action.Action;
import org.jhotdraw8.collection.key.Key;
import org.jhotdraw8.tree.PreorderSpliterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * AbstractActivity.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractActivity extends AbstractDisableable implements Activity {

    protected final @NonNull ObjectProperty<Application> application = new SimpleObjectProperty<>(this, APPLICATION_PROPERTY);
    protected final @NonNull ObservableMap<Key<?>, Object> properties = FXCollections.observableHashMap();
    /**
     * The title of {@link Stage} that contains the activity will be bound to the title of the activity.
     */
    protected final @NonNull StringProperty title = new SimpleStringProperty(this, TITLE_PROPERTY,
            ApplicationLabels.getResources().getString("unnamedFile"));
    private final @NonNull IntegerProperty disambiguation = new SimpleIntegerProperty(this, DISAMBIGUATION_PROPERTY);
    private final @NonNull ReadOnlyMapProperty<String, Action> actions = new ReadOnlyMapWrapper<String, Action>(FXCollections.observableMap(new LinkedHashMap<>())).getReadOnlyProperty();


    public AbstractActivity() {
    }

    @Override
    public @NonNull IntegerProperty disambiguationProperty() {
        return disambiguation;
    }

    protected abstract void initActions(@NonNull ObservableMap<String, Action> actionMap);

    protected abstract void initView();

    @Override
    public @NonNull StringProperty titleProperty() {
        return title;
    }

    @Override
    public @NonNull ObjectProperty<Application> applicationProperty() {
        return application;
    }

    @Override
    public @NonNull ObservableMap<Key<?>, Object> getProperties() {
        return properties;
    }

    @Override
    public void destroy() {
        getNode().disableProperty().unbind();

        // We must unlink the entire scene because there is a memory
        // leaks in Scene.dirtyNodes
        // https://bugs.openjdk.java.net/browse/JDK-8274022
        // https://bugs.openjdk.java.net/browse/JDK-8269907
        List<Node> nodes = new ArrayList<>();
        new PreorderSpliterator<>(n ->
                (n instanceof Parent) ? ((Parent) n).getChildrenUnmodifiable() : Collections.emptyList(),
                getNode()).forEachRemaining(nodes::add);
        for (Node node : nodes) {
            if (node instanceof TableView) {
                ((TableView<?>) node).setItems(FXCollections.emptyObservableList());
                ((TableView<?>) node).setSelectionModel(null);
            } else if (node instanceof ListView) {
                ((ListView<?>) node).setItems(FXCollections.emptyObservableList());
            }
        }
        for (Node node : nodes) {
            if (node instanceof TableRow) {
                ((TableRow<?>) node).setItem(null);
            } else if (node instanceof ListCell) {
                ((ListCell<?>) node).setItem(null);
            } else if (node instanceof Pane) {
                ((Pane) node).getChildren().clear();
            }
        }


    }

    @Override
    public void stop() {
    }

    @Override
    public void start() {
    }

    public void init() {
        initView();
        initTitle();
        initActions(getActions());
        getNode().disableProperty().bind(disabledProperty());
    }

    protected abstract void initTitle();

    @Override
    public @NonNull ReadOnlyMapProperty<String, Action> actionsProperty() {
        return actions;
    }
}
