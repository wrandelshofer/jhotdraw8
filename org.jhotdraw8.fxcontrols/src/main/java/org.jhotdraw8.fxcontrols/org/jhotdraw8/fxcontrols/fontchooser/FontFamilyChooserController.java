/*
 * @(#)FontFamilyChooserController.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcontrols.fontchooser;


import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.application.resources.ModulepathResources;
import org.jhotdraw8.application.resources.Resources;

import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

public class FontFamilyChooserController {

    @FXML
    private Button addCollectionButton;
    @FXML
    private ListView<FontCollection> collectionList;
    @FXML
    private ListView<FontFamily> familyList;
    @FXML
    private Label fontNameLabel;
    @FXML
    private URL location;

    private final ObjectProperty<FontChooserModel> model = new SimpleObjectProperty<>();

    private final ObjectProperty<EventHandler<ActionEvent>> onAction = new SimpleObjectProperty<>();

    protected final DoubleProperty fontSize = new SimpleDoubleProperty(13.0);

    @FXML
    private TextArea previewTextArea;
    @FXML
    private ResourceBundle resources;

    @FXML
    private ListView<FontTypeface> typefaceList;

    public FontFamilyChooserController() {
    }


    public double getFontSize() {
        return fontSize.get();
    }

    public FontChooserModel getModel() {
        return model.get();
    }

    public void setFontSize(double size) {
        fontSize.set(size);
    }

    public void setModel(FontChooserModel value) {
        model.set(value);
    }

    public EventHandler<ActionEvent> getOnAction() {
        return onAction.get();
    }

    public void setOnAction(EventHandler<ActionEvent> value) {
        onAction.set(value);
    }

    public @Nullable String getSelectedFontName() {
        FontTypeface typeface = typefaceList == null ? null : typefaceList.getSelectionModel().getSelectedItem();
        return typeface == null ? null : typeface.getName();
    }

    private void initDoubleClickBehavior() {
        final EventHandler<MouseEvent> onMouseHandler = evt -> {
            if (evt.getClickCount() == 2 && getOnAction() != null && getSelectedFontName() != null) {
                getOnAction().handle(new ActionEvent(evt.getSource(), evt.getTarget()));
            }
        };
        typefaceList.setOnMousePressed(onMouseHandler);
        familyList.setOnMousePressed(onMouseHandler);
    }

    private void initListCells() {
        familyList.setCellFactory(lv -> {
            final TextFieldListCell<FontFamily> listCell = new TextFieldListCell<>();
            return listCell;
        });

        collectionList.setCellFactory(lv -> {
            final TextFieldListCell<FontCollection> listCell = new TextFieldListCell<FontCollection>();
            return listCell;
        });
    }

    private void initListSelectionBehavior() throws MissingResourceException {
        final Resources labels = ModulepathResources.getResources(FontDialog.class.getModule(), "org.jhotdraw8.fxcontrols.spi.labels");

        collectionList.getSelectionModel().selectedItemProperty().addListener((o, oldv, newv) -> {
            familyList.setItems(newv == null ? null : newv.getFamilies());
            if (!familyList.getItems().isEmpty()) {
                familyList.getSelectionModel().select(0);
            }
        });
        familyList.getSelectionModel().selectedItemProperty().addListener((o, oldv, newv) -> {
            typefaceList.setItems(newv == null ? null : newv.getTypefaces());
            if (newv != null && !newv.getTypefaces().isEmpty()) {
                final ObservableList<FontTypeface> items = typefaceList.getItems();
                boolean found = false;
                for (int i = 0, n = items.size(); i < n; i++) {
                    if (items.get(i).isRegular()) {
                        typefaceList.getSelectionModel().select(i);
                        found = true;
                        break;
                    }
                }
                if (!found && !typefaceList.getItems().isEmpty()) {
                    typefaceList.getSelectionModel().select(0);
                }
            }
        });
        typefaceList.getSelectionModel().selectedItemProperty().addListener((o, oldv, newv) -> {
            if (newv == null) {
                fontNameLabel.setText(labels.getString("FontChooser.nothingSelected"));
            } else {
                fontNameLabel.setText(newv.getName());
            }
            updatePreviewTextArea();
        });
    }

    protected void updatePreviewTextArea() {
        String text = fontNameLabel.getText();
        final Resources labels = ModulepathResources.getResources(FontDialog.class.getModule(), "org.jhotdraw8.fxcontrols.spi.labels");
        if (text == null || text.equals(labels.getString("FontChooser.nothingSelected"))) {
            previewTextArea.setFont(new Font("System Regular", getFontSize()));
        } else {
            previewTextArea.setFont(new Font(text, getFontSize()));
        }
    }

    private void initPreferencesBehavior() {
        Preferences prefs = Preferences.userNodeForPackage(FontFamilyChooserController.class);
        previewTextArea.setText(prefs.get("fillerText", "Now is the time for all good men."));
        previewTextArea.textProperty().addListener((o, oldv, newv) -> prefs.put("fillerText", newv));
    }

    private void initUpdateViewFromModelBehavior() {
        model.addListener((o, oldv, newv) -> {
            if (oldv != null) {
                collectionList.itemsProperty().unbind();
            }
            if (newv != null) {
                collectionList.itemsProperty().bind(newv.fontCollectionsProperty());
            }
        });
    }

    @FXML
    void initialize() {
        assert previewTextArea != null : "fx:id=\"previewTextArea\" was not injected: check your FXML file 'FontChooser.fxml'.";
        assert fontNameLabel != null : "fx:id=\"fontNameLabel\" was not injected: check your FXML file 'FontChooser.fxml'.";
        assert addCollectionButton != null : "fx:id=\"addCollectionButton\" was not injected: check your FXML file 'FontChooser.fxml'.";
        assert collectionList != null : "fx:id=\"collectionList\" was not injected: check your FXML file 'FontChooser.fxml'.";
        assert familyList != null : "fx:id=\"familyList\" was not injected: check your FXML file 'FontChooser.fxml'.";
        assert typefaceList != null : "fx:id=\"typefaceList\" was not injected: check your FXML file 'FontChooser.fxml'.";

        initUpdateViewFromModelBehavior();
        initListSelectionBehavior();
        initDoubleClickBehavior();
        initPreferencesBehavior();
        initListCells();

        collectionList.itemsProperty().addListener((o, oldv, newv) -> {
            System.out.println("collectionList change=" + newv);
        });

    }

    public @NonNull ObjectProperty<FontChooserModel> modelProperty() {
        return model;
    }

    public @NonNull ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }

    public void setFontName(String fontName) {
        final ObservableList<FontCollection> collections = collectionList.getItems();
        for (int i = 0, n = collections.size(); i < n; i++) {
            final FontCollection fontCollection = collections.get(i);
            final ObservableList<FontFamily> families = fontCollection.getFamilies();
            for (int j = 0, m = families.size(); j < m; j++) {
                final FontFamily fontFamily = families.get(j);
                final ObservableList<FontTypeface> typefaces = fontFamily.getTypefaces();
                for (int k = 0, p = typefaces.size(); k < p; k++) {
                    final FontTypeface fontTypeface = typefaces.get(k);
                    if (fontTypeface.getName().equals(fontName)) {
                        collectionList.getSelectionModel().select(i);
                        familyList.getSelectionModel().select(j);
                        typefaceList.getSelectionModel().select(k);
                        break;
                    }
                }
            }
        }
    }


}
