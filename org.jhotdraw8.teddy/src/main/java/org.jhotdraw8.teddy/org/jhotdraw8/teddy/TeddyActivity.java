/*
 * @(#)TeddyActivity.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.teddy;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.TextArea;
import javafx.scene.input.DataFormat;
import javafx.scene.text.Font;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.application.AbstractFileBasedActivity;
import org.jhotdraw8.application.Application;
import org.jhotdraw8.application.FileBasedActivity;
import org.jhotdraw8.application.action.Action;
import org.jhotdraw8.application.action.edit.RedoAction;
import org.jhotdraw8.application.action.edit.UndoAction;
import org.jhotdraw8.collection.immutable.ImmutableMap;
import org.jhotdraw8.fxbase.concurrent.FXWorker;
import org.jhotdraw8.fxbase.concurrent.WorkState;
import org.jhotdraw8.fxbase.control.TextInputControlUndoAdapter;
import org.jhotdraw8.fxbase.undo.FXUndoManager;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.teddy.action.FontAction;
import org.jhotdraw8.teddy.action.FontableActivity;

import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;

/**
 * TeddyActivityController.
 *
 * @author Werner Randelshofer
 */
public class TeddyActivity extends AbstractFileBasedActivity implements FileBasedActivity, Initializable, FontableActivity {

    @FXML
    private TextArea textArea;

    private FXUndoManager undoManager = new FXUndoManager();

    @Override
    public @NonNull CompletionStage<Void> clear() {
        textArea.setText(null);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void clearModified() {
        modified.set(false);
    }

    @Override
    public Node getNode() {
        return textArea;
    }

    @Override
    protected void initActions(@NonNull ObservableMap<String, Action> map) {
        super.initActions(map);
        final Application app = getApplication();
        map.put(FontAction.ID, new FontAction(app, this));
        map.put(UndoAction.ID, new UndoAction(this, undoManager));
        map.put(RedoAction.ID, new RedoAction(this, undoManager));
    }

    @Override
    public void initView() {

    }

    /**
     * Initializes the controller class.
     */
    @FXML
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        textArea.textProperty().addListener((observable -> modified.set(true)));
        new TextInputControlUndoAdapter(textArea).addUndoEditListener(undoManager);
        undoManager.discardAllEdits();
    }

    @Override
    public @NonNull CompletionStage<Void> print(@NonNull PrinterJob job, @NonNull WorkState<Void> workState) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public @NonNull CompletionStage<DataFormat> read(
            @NonNull URI uri, DataFormat format,
            @NonNull ImmutableMap<Key<?>, Object> options,
            boolean insert,
            WorkState<Void> workState) {
        return FXWorker.supply(Executors.newSingleThreadExecutor(), () -> {
            StringBuilder builder = new StringBuilder();
            char[] cbuf = new char[8192];
            try (Reader in = Files.newBufferedReader(Paths.get(uri))) {
                for (int count = in.read(cbuf, 0, cbuf.length); count != -1; count = in.read(cbuf, 0, cbuf.length)) {
                    builder.append(cbuf, 0, count);
                }
            }
            return builder.toString();
        }).thenApply(value -> {
            if (insert) {
                textArea.insertText(textArea.getCaretPosition(), value);
            } else {
                textArea.setText(value);
            }
            return format;
        });
    }

    @Override
    public ObjectProperty<Font> fontProperty() {
        return textArea.fontProperty();
    }

    @Override
    public @NonNull CompletionStage<Void> write(
            @NonNull URI uri, DataFormat format,
            @NonNull ImmutableMap<Key<?>, Object> options,
            @NonNull WorkState<Void> workState) {
        final String text = textArea.getText();
        return FXWorker.run(Executors.newSingleThreadExecutor(), () -> {
            try (Writer out = Files.newBufferedWriter(Paths.get(uri))) {
                out.write(text);
            }
        });
    }

}
