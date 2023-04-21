/*
 * @(#)StyleClassCell.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.inspector;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * FXML Controller class.
 * <p>
 * XXX all keys must be customizable
 *
 * @author Werner Randelshofer
 */
public class StyleClassCell extends ListCell<StyleClassItem> {

    private HBox node;
    private @Nullable StyleClassItem item;
    private boolean isUpdating;

    @FXML
    private Button removeButton;
    private final StyleClassesInspector inspector;

    public StyleClassCell(StyleClassesInspector inspector) {
        this(LayersInspector.class.getResource("StyleClassCell.fxml"), inspector);
    }

    public StyleClassCell(@NonNull URL fxmlUrl, StyleClassesInspector inspector) {
        init(fxmlUrl);
        this.inspector = inspector;
    }

    private void init(@NonNull URL fxmlUrl) {
        FXMLLoader loader = new FXMLLoader();
        loader.setController(this);
        loader.setResources(InspectorLabels.getResources().asResourceBundle());

        try (InputStream in = fxmlUrl.openStream()) {
            node = loader.load(in);

            removeButton.addEventHandler(ActionEvent.ACTION, o -> {
                inspector.removeTag(item.getText());
            });

        } catch (IOException ex) {
            throw new InternalError(ex);
        }
    }

    @Override
    protected void updateItem(@Nullable StyleClassItem item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            this.item = null;
        } else {
            isUpdating = true;
            this.item = item;
            setText(item.isInAllElements() ? item.getText() : "(" + item.getText() + ")");
            setGraphic(node);
        }
    }

    public static @NonNull Callback<ListView<StyleClassItem>, ListCell<StyleClassItem>> forListView(StyleClassesInspector inspector) {
        return list -> new StyleClassCell(inspector);
    }

}
