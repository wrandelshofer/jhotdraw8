/*
 * @(#)RevertFileAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action.file;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.DataFormat;
import org.jhotdraw8.application.ApplicationLabels;
import org.jhotdraw8.application.FileBasedActivity;
import org.jhotdraw8.application.action.AbstractActivityAction;
import org.jhotdraw8.fxbase.concurrent.SimpleWorkState;
import org.jhotdraw8.fxbase.concurrent.WorkState;
import org.jhotdraw8.icollection.ChampMap;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lets the user write unsaved changes of the active view, then presents an
 * {@code URIChooser} and loads the selected URI into the active view.
 *
 * @author Werner Randelshofer
 */
public class RevertFileAction extends AbstractActivityAction<FileBasedActivity> {

    public static final String ID = "file.revert";

    /**
     * Creates a new instance.
     *
     * @param view the view
     */
    @SuppressWarnings("this-escape")
    public RevertFileAction(FileBasedActivity view) {
        super(view);
        ApplicationLabels.getResources().configureAction(this, ID);
    }

    @Override
    protected void onActionPerformed(ActionEvent event, FileBasedActivity activity) {
        if (isDisabled()) {
            return;
        }
        final URI uri = activity.getURI();
        final DataFormat dataFormat = activity.getDataFormat();
        if (activity.isModified()) {
            Alert alert = new Alert(Alert.AlertType.WARNING,
                    ApplicationLabels.getResources().getString("file.revert.doYouWantToRevert.message"), ButtonType.YES, ButtonType.CANCEL);
            alert.getDialogPane().setMaxWidth(640.0);
            Optional<ButtonType> answer = alert.showAndWait();
            if (answer.isPresent() && answer.get() == ButtonType.YES) {
                doIt(activity, uri, dataFormat);
            }
        } else {
            doIt(activity, uri, dataFormat);
        }
    }

    private void doIt(FileBasedActivity view, @Nullable URI uri, DataFormat dataFormat) {
        WorkState<Void> workState = new SimpleWorkState<>(getLabel());
        view.addDisabler(workState);

        final BiFunction<DataFormat, Throwable, Void> handler = (actualDataFormat, throwable) -> {
            if (throwable != null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, createErrorMessage(throwable));
                alert.getDialogPane().setMaxWidth(640.0);
                alert.showAndWait();
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unexpected Exception " + throwable.getMessage(), throwable);

            }
            view.clearModified();
            view.removeDisabler(workState);
            return null;
        };

        if (uri == null) {
            view.clear().handle((ignored, throwable) -> handler.apply(null, throwable));
        } else {
            view.read(uri, dataFormat,
                            ChampMap.of(), false, workState)
                    .handle(handler);
        }
    }

}
