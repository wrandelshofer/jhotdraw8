/*
 * @(#)AbstractDrawingView.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlySetProperty;
import javafx.beans.property.ReadOnlySetWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.Layer;
import org.jhotdraw8.draw.handle.Handle;
import org.jhotdraw8.draw.input.ClipboardInputFormat;
import org.jhotdraw8.draw.input.ClipboardOutputFormat;
import org.jhotdraw8.draw.io.BitmapExportOutputFormat;
import org.jhotdraw8.draw.model.DrawingModel;
import org.jhotdraw8.draw.tool.Tool;
import org.jhotdraw8.fxbase.beans.AbstractPropertyBean;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.SequencedMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AbstractDrawingView.
 *
 */
public abstract class AbstractDrawingView extends AbstractPropertyBean implements DrawingView {
    private final ObjectProperty<ClipboardOutputFormat> clipboardOutputFormat = new SimpleObjectProperty<>(this, CLIPBOARD_OUTPUT_FORMAT_PROPERTY,
            new BitmapExportOutputFormat(BitmapExportOutputFormat.PNG_MIME_TYPE, BitmapExportOutputFormat.PNG_EXTENSION));
    private final ObjectProperty<DrawingEditor> editor = new SimpleObjectProperty<>(this, EDITOR_PROPERTY, null);
    private final ObjectProperty<ClipboardInputFormat> clipboardInputFormat = new SimpleObjectProperty<>(this, CLIPBOARD_INPUT_FORMAT_PROPERTY);
    /**
     * The selectedFiguresProperty holds the list of selected figures in the
     * sequence they were selected by the user.
     */
    private final ReadOnlySetProperty<Figure> selectedFigures = new ReadOnlySetWrapper<>(this, SELECTED_FIGURES_PROPERTY, FXCollections.observableSet(new LinkedHashSet<Figure>())).getReadOnlyProperty();

    private final ObjectProperty<Tool> tool = new SimpleObjectProperty<>(this, TOOL_PROPERTY);

    {
        tool.addListener(this::onToolChanged);
    }

    {
        InvalidationListener listener = observable -> {
            recreateHandles();
            invalidateHandles();
            repaint();
        };
        selectedFigures.addListener(listener);
    }

    private final ObjectProperty<Handle> activeHandle = new SimpleObjectProperty<>(this, ACTIVE_HANDLE_PROPERTY);

    public AbstractDrawingView() {
    }


    @Override
    public ObjectProperty<DrawingEditor> editorProperty() {
        return editor;
    }

    @Override
    public ObjectProperty<ClipboardInputFormat> clipboardInputFormatProperty() {
        return clipboardInputFormat;
    }

    @Override
    public ObjectProperty<ClipboardOutputFormat> clipboardOutputFormatProperty() {
        return clipboardOutputFormat;
    }

    public void cut() {
        copy();
        final List<Figure> selectedFigures = new ArrayList<>(getSelectedFigures());
        DrawingModel m = getModel();
        for (Figure f : selectedFigures) {
            if (f.isDeletable()) {
                for (Figure d : f.preorderIterable()) {
                    m.disconnect(d);
                }
                m.removeFromParent(f);
            }
        }
    }

    public void copy() {
        ClipboardOutputFormat out = getClipboardOutputFormat();
        Drawing drawing = getDrawing();
        if (out == null || drawing == null) {
            return;
        } 
            SequencedMap<DataFormat, Object> content = new LinkedHashMap<>() {
                @Serial
                private static final long serialVersionUID = 0L;

                @Override
                public Object put(@Nullable DataFormat key, Object value) {
                    Objects.requireNonNull(key, "key");
                    return super.put(key, value);
                }

            };
            try {
                final ObservableSet<Figure> selectedFigures = getSelectedFigures();

                out.write(content, drawing, selectedFigures.isEmpty() ? FXCollections.singletonObservableList(drawing) : selectedFigures);
                Clipboard.getSystemClipboard().setContent(content);
            } catch (IOException ex) {
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "Could not copy to clipboard.", ex);
            }

    }

    public void paste() {
        // Only paste if there is an editable parent.
        Figure parent = getActiveParent();
        if (parent == null || !parent.isEditable() || parent.getDrawing() != getDrawing()) {
            parent = null;
            for (Figure f : getDrawing().getChildren()) {
                if (f.isEditable() && (f instanceof Layer)) {
                    parent = f;
                }
            }
            if (parent == null) {
                return;// FIXME should create a layer with the editor
            }
        }
        Clipboard cb = Clipboard.getSystemClipboard();
        ClipboardInputFormat in = getClipboardInputFormat();
        if (in != null) {
            try {
                Set<Figure> pastedFigures = in.read(cb, getModel(), getDrawing(), parent);
                getSelectedFigures().clear();
                if (pastedFigures != null) {
                    getSelectedFigures().addAll(pastedFigures);
                }
            } catch (IOException ex) {
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unexpected Exception " + ex.getMessage(), ex);

            }
        }
    }

    @Override
    public ObjectProperty<Tool> toolProperty() {
        return tool;
    }

    @Override
    public ObjectProperty<Handle> activeHandleProperty() {
        return activeHandle;
    }


    @Override
    public ReadOnlySetProperty<Figure> selectedFiguresProperty() {
        return selectedFigures;
    }

    protected abstract void invalidateHandles();

    protected abstract void repaint();

    protected abstract void onToolChanged(Observable observable, @Nullable Tool oldValue, @Nullable Tool newValue);

    @Override
    public <T> void set(MapAccessor<T> key, @Nullable T value) {
        key.set(properties, value);
    }
}
