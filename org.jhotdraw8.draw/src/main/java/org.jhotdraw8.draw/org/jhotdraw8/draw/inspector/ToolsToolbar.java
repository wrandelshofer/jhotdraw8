/*
 * @(#)ToolsToolbar.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.inspector;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import org.jhotdraw8.draw.DrawingEditor;
import org.jhotdraw8.draw.tool.Tool;

/**
 * FXML Controller class
 *
 */
public class ToolsToolbar extends GridPane {

    private final ToggleGroup group = new ToggleGroup();
    private final ObjectProperty<DrawingEditor> editor = new SimpleObjectProperty<>(this, "editor");

    {
        group.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && getDrawingEditor() != null) {
                @SuppressWarnings("unchecked")
                Tool userData = (Tool) newValue.getUserData();
                getDrawingEditor().setActiveTool(userData);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private final ChangeListener<Tool> activeToolHandler = (o, oldValue, newValue) -> {

        for (Toggle button : group.getToggles()) {
            if (button.getUserData() == newValue) {
                button.setSelected(true);
                break;
            }
        }
    };

    {
        editor.addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.activeToolProperty().removeListener(activeToolHandler);
            }
            if (newValue != null) {
                newValue.activeToolProperty().addListener(activeToolHandler);
                if (group.getSelectedToggle() != null) {
                    newValue.setActiveTool(((Tool) group.getSelectedToggle().getUserData()));
                }
            }
        });
    }

    public ToolsToolbar(DrawingEditor editor) {
        setDrawingEditor(editor);
    }

    public ToggleButton addTool(Tool tool, int gridx, int gridy) {
        return addTool(tool, gridx, gridy, 0);
    }

    public ToggleButton addTool(Tool tool, int gridx, int gridy, double marginLeft) {
        ToggleButton button = new ToggleButton();
        if (tool.get(Tool.LARGE_ICON_KEY) != null) {
            button.setGraphic(tool.get(Tool.LARGE_ICON_KEY));
            if (tool.get(Tool.SHORT_DESCRIPTION) != null) {
                button.setTooltip(new Tooltip(tool.get(Tool.SHORT_DESCRIPTION)));
            }
        } else {
            button.setText(tool.getName());
        }
        String styleClass = tool.get(Tool.STYLE_CLASS_KEY);
        if (styleClass != null) {
            button.getStyleClass().add(styleClass);
        }
        button.setFocusTraversable(false);
        button.setUserData(tool);
        if (group.getToggles().isEmpty()) {
            button.setSelected(true);
        }
        group.getToggles().add(button);
        add(button, gridx, gridy);
        GridPane.setMargin(button, new Insets(0, 0, 0, marginLeft));
        return button;
    }

    public ObjectProperty<DrawingEditor> drawingEditor() {
        return editor;
    }

    public DrawingEditor getDrawingEditor() {
        return editor.get();
    }

    public void setDrawingEditor(DrawingEditor editor) {
        this.editor.set(editor);
    }
}
