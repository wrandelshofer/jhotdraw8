/*
 * @(#)ToggleBooleanAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action.view;

import javafx.beans.property.BooleanProperty;
import javafx.event.ActionEvent;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.application.Activity;
import org.jhotdraw8.application.action.AbstractActivityAction;
import org.jhotdraw8.application.resources.Resources;

/**
 * This action toggles the state of its boolean property.
 *
 * @author Werner Randelshofer
 */
public class ToggleBooleanAction extends AbstractActivityAction<Activity> {
    private final BooleanProperty value;

    public ToggleBooleanAction(@NonNull Activity activity, @Nullable String id, @Nullable Resources labels, BooleanProperty value) {
        super(activity);
        if (labels != null && id != null) {
            labels.configureAction(this, id);
        }
        this.value = value;
        selectedProperty().bind(value);
    }

    @Override
    protected void onActionPerformed(@NonNull ActionEvent event, @NonNull Activity activity) {
        value.set(!value.get());
    }
}
