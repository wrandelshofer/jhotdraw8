/*
 * @(#)InputDialog.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.control;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import org.jhotdraw8.annotation.NonNull;

import java.util.function.Supplier;

/**
 * This class is similar to TextInputDialog, but allows to specify an arbitrary JavaFX node as input form.
 *
 * @param <R> the return type of the dialog
 * @author Werner Randelshofer
 */
public class InputDialog<R> extends Dialog<R> {

    private Node inputForm;
    private Supplier<R> resultSupplier;

    /**
     * Creates a new InputDialog.
     *
     * @param title          the title
     * @param headerText     the header text
     * @param inputForm      the input form
     * @param resultSupplier the result supplier
     */
    public InputDialog(String title, String headerText, Node inputForm, @NonNull Supplier<R> resultSupplier) {
        final DialogPane dialogPane = getDialogPane();

        // -- textfield
        this.inputForm = inputForm;


        dialogPane.contentTextProperty().addListener(o -> updateGrid());

        setTitle(title);
        dialogPane.setHeaderText(headerText);
        dialogPane.getStyleClass().add("text-input-dialog");
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        updateGrid();

        setResultConverter((dialogButton) -> {
            ButtonBar.ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
            //noinspection ReturnOfNull
            return data == ButtonBar.ButtonData.OK_DONE ? resultSupplier.get() : null;
        });
    }

    /**
     * Returns the input form used within this dialog.
     *
     * @return the input form
     */
    public final @NonNull Node getInputForm() {
        return inputForm;
    }


    /**
     * Sets the input form used within this dialog.
     *
     * @param newValue the new input form
     */
    public final void setInputForm(Node newValue) {
        inputForm = newValue;
        updateGrid();
    }

    public Supplier<R> getResultSupplier() {
        return resultSupplier;
    }

    public void setResultSupplier(Supplier<R> resultSupplier) {
        this.resultSupplier = resultSupplier;
    }

    private void updateGrid() {
        getDialogPane().setContent(inputForm);

        Platform.runLater(() -> inputForm.requestFocus());
    }
}

