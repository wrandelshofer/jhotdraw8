/*
 * @(#)AbstractApplicationAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import org.jhotdraw8.application.Application;
import org.jhotdraw8.fxbase.control.Disableable;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * This abstract class can be extended to implement an {@code Action} that acts
 * on an {@link Application}.
 * <p>
 * An AbstractApplicationAction is disabled when it has disablers
 * {@link Disableable} or when its application is disabled.
 *
 * @author Werner Randelshofer.
 */
public abstract class AbstractApplicationAction extends AbstractAction {

    protected final Application app;

    /**
     * Creates a new instance.
     *
     * @param app the application
     */
    public AbstractApplicationAction(Application app) {
        Objects.requireNonNull(app, "app");
        this.app = app;
        disabled.unbind();
        disabled.bind(Bindings.isNotEmpty(disablers).or(app.disabledProperty()));
    }

    protected String createErrorMessage(@Nullable Throwable t) {
        StringBuilder buf = new StringBuilder();
        for (; t != null; t = t.getCause()) {
            if (t.getCause() != null && (t instanceof RuntimeException || t instanceof ExecutionException
            )) {
                continue;
            }

            final String msg = t.getLocalizedMessage();
            if (buf.indexOf(msg) < 0) {
                if (!buf.isEmpty()) {
                    buf.append('\n');
                }
                buf.append(msg == null ? t.toString() : t.getClass().getSimpleName() + ": " + msg);
            }
        }
        return buf.toString();
    }

    public final Application getApplication() {
        return app;
    }

    @Override
    protected final void onActionPerformed(ActionEvent event) {
        onActionPerformed(event, app);
    }

    /**
     * This method is invoked when the action is not disabled and the event is
     * not consumed.
     *
     * @param event the action event
     * @param app   the applicatoin
     */
    protected abstract void onActionPerformed(ActionEvent event, Application app);

    protected Alert createAlert(Alert.AlertType alertType, String message, String headerText) {
        TextArea textArea = new TextArea(message);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        GridPane gridPane = new GridPane();
        gridPane.add(textArea, 0, 0);

        Alert alert = new Alert(alertType);
        alert.getDialogPane().setContent(gridPane);
        alert.setHeaderText(headerText);
        alert.getDialogPane().setMaxWidth(640.0);
        return alert;
    }
}
