/*
 * @(#)CssColorDialog.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.popup;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.jspecify.annotations.Nullable;
import org.jhotdraw8.application.ApplicationLabels;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.draw.css.value.NamedCssColor;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CssColorDialog {
    private final ObjectProperty<CssColor> currentColor = new SimpleObjectProperty<>(NamedCssColor.WHITE);
    private final ObjectProperty<CssColor> customColor = new SimpleObjectProperty<>(NamedCssColor.TRANSPARENT);
    final @Nullable ButtonType chooseButtonType;
    final @Nullable ButtonType cancelButtonType;
    private CssColorChooserController controller;
    private Dialog<ButtonType> dialog;


    private Runnable onSave;
    private Runnable onUse;
    private Runnable onCancel;

    public CssColorDialog(Window owner) {
        final Resources labels = ApplicationLabels.getGuiResources();
        dialog = new Dialog<>();
        final DialogPane dialogPane = dialog.getDialogPane();
        try {

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("CssColorChooser.fxml"));
            loader.setResources(labels.asResourceBundle());
            loader.load();
            final Parent root = loader.getRoot();
            dialogPane.setContent(root);

            controller = loader.getController();
            currentColor.bindBidirectional(controller.colorProperty());
        } catch (IOException ex) {
            dialogPane.setContent(new Label(ex.getMessage()));
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unexpected Exception " + ex.getMessage(), ex);

        }

        dialog.setResizable(false);
//        dialog.initModality(Modality.WINDOW_MODAL); // FIXME does not work on macOS
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setResizable(false);

        //dialog.addEventHandler(KeyEvent.ANY, keyEventListener);

        chooseButtonType = new ButtonType(labels.getTextProperty("FontChooser.choose"), ButtonBar.ButtonData.OK_DONE);
        cancelButtonType = new ButtonType(labels.getTextProperty("FontChooser.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().setAll(chooseButtonType, cancelButtonType);

    }

    public @Nullable ObjectProperty<CssColor> currentColorProperty() {
        return currentColor;
    }

    public @Nullable ObjectProperty<CssColor> customColorProperty() {
        return customColor;
    }

    public CssColor getCurrentColor() {
        return currentColor.get();
    }

    public void setCurrentColor(CssColor currentColor) {
        this.currentColor.set(currentColor);
    }

    public CssColor getCustomColor() {
        return customColor.get();
    }

    public void setCustomColor(CssColor customColor) {
        this.customColor.set(customColor);
    }

    public Runnable getOnCancel() {
        return onCancel;
    }

    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    public Runnable getOnSave() {
        return onSave;
    }

    public void setOnSave(Runnable onSave) {
        this.onSave = onSave;
    }

    public Runnable getOnUse() {
        return onUse;
    }

    public void setOnUse(Runnable onUse) {
        this.onUse = onUse;
    }

    public void show() {
        final Optional<ButtonType> value = dialog.showAndWait();
        if (value.orElse(null) == chooseButtonType) {
            final Runnable onSave = getOnSave();
            if (onSave != null) {
                onSave.run();
            }
        }
    }

}
