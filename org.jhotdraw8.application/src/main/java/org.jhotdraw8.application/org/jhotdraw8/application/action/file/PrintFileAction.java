/*
 * @(#)PrintFileAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action.file;

import javafx.event.ActionEvent;
import javafx.print.PrinterJob;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.application.ApplicationLabels;
import org.jhotdraw8.application.FileBasedActivity;
import org.jhotdraw8.application.action.AbstractActivityAction;
import org.jhotdraw8.fxbase.concurrent.SimpleWorkState;
import org.jhotdraw8.fxbase.concurrent.WorkState;

/**
 * Presents a printer chooser to the user and then prints the
 * {@link FileBasedActivity}.
 * <p>
 * This action requires that the view implements the {@code PrintableView}
 * interface.
 *
 * @author Werner Randelshofer
 */
public class PrintFileAction extends AbstractActivityAction<FileBasedActivity> {

    public static final String ID = "file.print";


    /**
     * Creates a new instance.
     *
     * @param activity the activity
     */
    public PrintFileAction(@NonNull FileBasedActivity activity) {
        super(activity);
        ApplicationLabels.getResources().configureAction(this, ID);
    }

    @Override
    protected void onActionPerformed(@NonNull ActionEvent event, @NonNull FileBasedActivity activity) {
        WorkState<Void> workState = new SimpleWorkState<>();
        activity.addDisabler(workState);
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(activity.getNode().getScene().getWindow())) {
            activity.print(job, workState).thenRun(() -> activity.removeDisabler(workState));
        } else {
            Alert alert = new Alert(AlertType.INFORMATION, "Sorry, no printer found");
            alert.getDialogPane().setMaxWidth(640.0);
            alert.show();
            activity.removeDisabler(workState);
        }
    }
}
