/*
 * @(#)ToggleViewPropertyAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action.view;

import javafx.beans.property.BooleanProperty;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.application.Activity;
import org.jhotdraw8.application.Application;
import org.jhotdraw8.application.action.AbstractActivityAction;
import org.jhotdraw8.application.resources.Resources;

import java.util.function.Function;

/**
 * ToggleViewPropertyAction.
 *
 * @author Werner Randelshofer
 */
public class ToggleViewPropertyAction extends AbstractActivityAction<Activity> {

    private final @Nullable BooleanProperty property;
    private final @Nullable Function<Activity, Node> nodeGetter;

    @SuppressWarnings("this-escape")
    public ToggleViewPropertyAction(@NonNull Application app, Activity view, @NonNull BooleanProperty property, String id, @NonNull Resources labels) {
        super(view);
        labels.configureAction(this, id);
        this.property = property;
        this.nodeGetter = null;
        selectedProperty().bindBidirectional(property);
    }

    @SuppressWarnings("this-escape")
    public ToggleViewPropertyAction(@NonNull Application app, Activity view, @Nullable Function<Activity, Node> nodeGetter, String id, @NonNull Resources labels) {
        super(view);
        labels.configureAction(this, id);
        this.property = null;
        this.nodeGetter = nodeGetter;
    }

    @Override
    protected void onActionPerformed(@NonNull ActionEvent event, @NonNull Activity activity) {
        if (property != null) {
            property.set(!property.get());
        } else {
            Node node = nodeGetter.apply(activity);
            node.setVisible(!node.isVisible());
            this.setSelected(node.isVisible());
        }
    }

}
