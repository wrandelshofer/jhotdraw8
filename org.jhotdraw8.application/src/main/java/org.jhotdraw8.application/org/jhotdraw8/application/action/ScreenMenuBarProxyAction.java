/*
 * @(#)ScreenMenuBarProxyAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action;

import javafx.beans.binding.Bindings;
import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.application.Application;
import org.jhotdraw8.fxcollection.typesafekey.Key;

/**
 * ScreenMenuBarProxyAction.
 *
 * @author Werner Randelshofer
 */
public class ScreenMenuBarProxyAction extends AbstractAction implements MapChangeListener<Key<?>, Object> {

    private final @NonNull Application app;
    private @Nullable Action currentAction;

    public ScreenMenuBarProxyAction(@NonNull Application app, String id) {
        this.app = app;
        set(ID_KEY, id);
        disabled.unbind();
        disabled.set(true);
        selectedProperty().set(false);

        app.activeActivityProperty().addListener((o, oldv, newv) -> {
            if (currentAction != null) {
                disabled.unbind();
                disabled.set(true);
                selectedProperty().unbind();
                selectedProperty().set(false);
                currentAction.getProperties().removeListener(this);
            }
            if (newv != null) {
                currentAction = newv.getActions().get(id);
            }
            Action currentAction1 = currentAction;
            if (currentAction1 != null) {
                disabled.bind(Bindings.isNotEmpty(disablers).or(currentAction1.disabledProperty()));
                selectedProperty().bind(currentAction1.selectedProperty());
                getProperties().clear();
                getProperties().putAll(currentAction1.getProperties());
                currentAction1.getProperties().addListener(this);
            }
        });
    }

    @Override
    protected void onActionPerformed(@NonNull ActionEvent event) {
        if (currentAction != null) {
            currentAction.handle(event);
        }
    }

    @Override
    public void onChanged(Change<? extends Key<?>, ?> change) {
        if (change.wasRemoved() & !change.wasAdded()) {
            getProperties().remove(change.getKey());
        }
        if (change.wasAdded()) {
            getProperties().put(change.getKey(), change.getValueAdded());
        }
    }
}
