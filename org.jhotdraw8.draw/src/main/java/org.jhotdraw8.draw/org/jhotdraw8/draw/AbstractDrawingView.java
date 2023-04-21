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
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * AbstractDrawingView.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractDrawingView extends AbstractPropertyBean implements DrawingView {

    private final @NonNull ObjectProperty<ClipboardOutputFormat> clipboardOutputFormat = new SimpleObjectProperty<>(this, CLIPBOARD_OUTPUT_FORMAT_PROPERTY, new BitmapExportOutputFormat());
    private final @NonNull ObjectProperty<DrawingEditor> editor = new SimpleObjectProperty<>(this, EDITOR_PROPERTY, null);
    private final @NonNull ObjectProperty<ClipboardInputFormat> clipboardInputFormat = new SimpleObjectProperty<>(this, CLIPBOARD_INPUT_FORMAT_PROPERTY);
    /**
     * The selectedFiguresProperty holds the list of selected figures in the
     * sequence they were selected by the user.
     */
    private final @NonNull ReadOnlySetProperty<Figure> selectedFigures = new ReadOnlySetWrapper<>(this, SELECTED_FIGURES_PROPERTY, FXCollections.observableSet(new LinkedHashSet<Figure>())).getReadOnlyProperty();

    private final @NonNull ObjectProperty<Tool> tool = new SimpleObjectProperty<>(this, TOOL_PROPERTY);

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

    private final @NonNull ObjectProperty<Handle> activeHandle = new SimpleObjectProperty<>(this, ACTIVE_HANDLE_PROPERTY);

    public AbstractDrawingView() {
    }


    @Override
    public @NonNull ObjectProperty<DrawingEditor> editorProperty() {
        return editor;
    }

    @Override
    public @NonNull ObjectProperty<ClipboardInputFormat> clipboardInputFormatProperty() {
        return clipboardInputFormat;
    }

    @Override
    public @NonNull ObjectProperty<ClipboardOutputFormat> clipboardOutputFormatProperty() {
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
        if (out == null) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        } else {
            Map<DataFormat, Object> content = new LinkedHashMap<DataFormat, Object>() {
                private static final long serialVersionUID = 0L;

                @Override
                public Object put(@Nullable DataFormat key, Object value) {
                    Objects.requireNonNull(key, "key");
                    return super.put(key, value);
                }

            };
            try {
                final ObservableSet<Figure> selectedFigures = getSelectedFigures();

                out.write(content, getDrawing(), selectedFigures.isEmpty() ? FXCollections.singletonObservableList(getDrawing()) : selectedFigures);
                Clipboard.getSystemClipboard().setContent(content);

            } catch (IOException ex) {
                ex.printStackTrace();
            }
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
                ex.printStackTrace();
            }
        }
    }

    @Override
    public @NonNull ObjectProperty<Tool> toolProperty() {
        return tool;
    }

    @Override
    public @NonNull ObjectProperty<Handle> activeHandleProperty() {
        return activeHandle;
    }


    @Override
    public @NonNull ReadOnlySetProperty<Figure> selectedFiguresProperty() {
        return selectedFigures;
    }

    protected abstract void invalidateHandles();

    protected abstract void repaint();

    protected abstract void onToolChanged(@NonNull Observable observable, @Nullable Tool oldValue, @Nullable Tool newValue);

    @Override
    public <T> void set(@NonNull MapAccessor<T> key, @Nullable T value) {
        key.set(properties, value);
    }
}
