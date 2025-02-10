/*
 * @(#)FontDialog.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcontrols.fontchooser;

import javafx.fxml.FXMLLoader;
import javafx.scene.AccessibleAction;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import org.jhotdraw8.application.resources.ModulepathResources;
import org.jhotdraw8.application.resources.Resources;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * FontDialog for selecting a font family and a font size.
 *
 */
public class FontDialog extends Dialog<FontFamilySize> {

    private FontChooserController controller;

    @SuppressWarnings("this-escape")
    public FontDialog() {
        final Resources labels = ModulepathResources.getResources(FontDialog.class.getModule(), "org.jhotdraw8.fxcontrols.spi.labels");
        final DialogPane dialogPane = getDialogPane();
        try {

            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(FontDialog.class.getResource("FontChooser.fxml"));
            loader.setResources(labels.asResourceBundle());
            loader.load();
            final Parent root = loader.getRoot();
            dialogPane.setContent(root);

            root.getStylesheets().add(FontDialog.class.getResource("fontchooser.css").toString());
            controller = loader.getController();
        } catch (IOException ex) {
            dialogPane.setContent(new Label(ex.getMessage()));
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unexpected Exception " + ex.getMessage(), ex);

        }

        setResizable(true);

        ButtonType chooseButtonType = new ButtonType(labels.getTextProperty("FontChooser.choose"), ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(labels.getTextProperty("FontChooser.cancel"), ButtonData.CANCEL_CLOSE);
        dialogPane.getButtonTypes().setAll(chooseButtonType, cancelButtonType);

        setResultConverter(this::onButton);
        controller.setOnAction(evt -> dialogPane.lookupButton(chooseButtonType).executeAccessibleAction(AccessibleAction.FIRE));

        controller.setModel(getModel());

    }

    /**
     * This model is shared by all font dialogs.
     */
    private static @Nullable FontChooserModel model = null;

    public static @Nullable FontChooserModel getModel() {
        if (model == null) {
            model = FontChooserModelFactories.create();
        }
        return model;
    }

    private @Nullable FontFamilySize onButton(@Nullable ButtonType buttonType) {
        if (buttonType != null && buttonType.getButtonData() == ButtonData.OK_DONE) {
            String selectedFontName = controller.getSelectedFontName();
            double fontSize = controller.getFontSize();
            return new FontFamilySize(selectedFontName, fontSize);
        } else {
            return null;
        }
    }

    public void selectFontName(String fontName) {
        controller.setFontName(fontName);
    }

    public final Optional<FontFamilySize> showAndWait(@Nullable FontFamilySize font) {
        if (font != null) {
            selectFontName(font.family());
            controller.setFontSize(font.size());
        }
        return showAndWait();
    }

}
