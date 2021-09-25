/*
 * @(#)AbstractActivity.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
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
import javafx.scene.layout.Pane;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.app.action.Action;
import org.jhotdraw8.collection.Key;
import org.jhotdraw8.tree.PostorderSpliterator;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * AbstractActivity.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractActivity extends AbstractDisableable implements Activity {

    protected final @NonNull ObjectProperty<Application> application = new SimpleObjectProperty<>(this, APPLICATION_PROPERTY);
    protected final @NonNull ObservableMap<Key<?>, Object> properties = FXCollections.observableHashMap();
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
        // XXX Unbind and destroy node hierarchy because of memory leak in JavaFX
        // https://bugs.openjdk.java.net/browse/JDK-8274022
        getNode().disableProperty().unbind();
        PostorderSpliterator<Node> spliterator = new PostorderSpliterator<>(n -> (n instanceof Parent) ? ((Parent) n).getChildrenUnmodifiable() : Collections.<Node>emptyList(), getNode());
        for (Node node : StreamSupport.stream(spliterator, false).collect(Collectors.toList())) {
            if (node instanceof Pane) {
                Pane pane = (Pane) node;
                pane.getChildren().clear();
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
