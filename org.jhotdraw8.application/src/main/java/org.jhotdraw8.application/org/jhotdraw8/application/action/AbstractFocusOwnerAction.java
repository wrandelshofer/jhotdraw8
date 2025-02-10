/*
 * @(#)AbstractFocusOwnerAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import org.jhotdraw8.application.Activity;
import org.jhotdraw8.application.Application;
import org.jspecify.annotations.Nullable;

/**
 * AbstractFocusOwnerAction.
 *
 */
public abstract class AbstractFocusOwnerAction extends AbstractApplicationAction {

    private @Nullable Node target = null;

    private final @Nullable ChangeListener<Activity> activeViewListener = (observable, oldValue, newValue) -> {
        disabled.unbind();
        if (newValue == null || newValue.getNode() == null) {
            disabled.set(true);
        } else {
            Scene s = newValue.getNode().getScene();
            BooleanBinding binding = Bindings.isNotEmpty(disablers).or(app.disabledProperty());
            if (target != null) {
                binding = binding.or(s.focusOwnerProperty().isNotEqualTo(target));
            } else {
                binding = binding.or(s.focusOwnerProperty().isNull());
            }
            disabled.bind(binding);
        }
    };

    /**
     * Creates a new instance.
     *
     * @param app the application
     */
    public AbstractFocusOwnerAction(Application app) {
        this(app, null);
    }

    /**
     * Creates a new instance.
     *
     * @param app    the application
     * @param target the target node
     */
    public AbstractFocusOwnerAction(Application app, @Nullable Node target) {
        super(app);
        this.target = target;

        app.activeActivityProperty().addListener(activeViewListener);
        activeViewListener.changed(null, null, app.getActiveActivity());

    }
}
