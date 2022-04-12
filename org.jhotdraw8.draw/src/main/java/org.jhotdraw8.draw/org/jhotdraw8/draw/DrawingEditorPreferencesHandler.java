/*
 * @(#)DrawingEditorPreferencesHandler.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw;

import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.css.CssColor;

import java.util.prefs.Preferences;

/**
 * Handles preferences for a drawing editor.
 */
public class DrawingEditorPreferencesHandler {
    private static final String DRAWING_EDITOR = "DrawingEditor.";
    private final @NonNull DrawingEditor editor;
    private final @NonNull Preferences prefs;
    private final @NonNull String prefix;

    /**
     * Creates a new handler for the specified editor. All preferences
     * have the prefix {@value #DRAWING_EDITOR}.
     *
     * @param editor the editor
     * @param prefs  the preferences
     */
    public DrawingEditorPreferencesHandler(@NonNull DrawingEditor editor, @NonNull Preferences prefs) {
        this(editor, prefs, DRAWING_EDITOR);
    }

    /**
     * Creates a new handler for the specified editor. All preferences
     * have the specified prefix.
     *
     * @param editor the editor
     * @param prefs  the preferences
     * @param prefix the prefix
     */
    public DrawingEditorPreferencesHandler(@NonNull DrawingEditor editor, @NonNull Preferences prefs, @NonNull String prefix) {
        this.editor = editor;
        this.prefs = prefs;
        this.prefix = prefix;

        readPreferences();

        final ChangeListener<Number> doublePropertyListener = (o, oldv, newv) -> {
            prefs.putDouble(prefix + ((Property<?>) o).getName(), newv.doubleValue());
        };
        final ChangeListener<CssColor> colorPropertyListener = (o, oldv, newv) -> {
            prefs.put(prefix + ((Property<?>) o).getName(), newv.getName());
        };
        editor.handleSizeProperty().addListener(doublePropertyListener);
        editor.handleStrokeWidthProperty().addListener(doublePropertyListener);
        editor.toleranceProperty().addListener(doublePropertyListener);
        editor.handleColorProperty().addListener(colorPropertyListener);
    }

    private void readPreferences() {
        editor.setHandleSize(prefs.getDouble(prefix + DrawingEditor.HANDLE_SIZE_PROPERTY, editor.getHandleSize()));
        editor.setTolerance(prefs.getDouble(prefix + DrawingEditor.TOLERANCE_PROPERTY, editor.getTolerance()));
        editor.setHandleStrokeWidth(prefs.getDouble(prefix + DrawingEditor.HANDLE_STROKE_WDITH_PROPERTY, editor.getHandleStrokeWidth()));
        editor.setHandleColor(CssColor.valueOf(prefs.get(prefix + DrawingEditor.HANDLE_COLOR_PROPERTY, editor.getHandleColor().getName())));

    }
}
