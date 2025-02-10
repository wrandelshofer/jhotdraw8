/*
 * @(#)AbstractSaveFileAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action.file;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.input.DataFormat;
import javafx.stage.Modality;
import org.jhotdraw8.application.ApplicationLabels;
import org.jhotdraw8.application.FileBasedActivity;
import org.jhotdraw8.application.action.AbstractActivityAction;
import org.jhotdraw8.application.controls.urichooser.FileURIChooser;
import org.jhotdraw8.application.controls.urichooser.URIChooser;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.base.net.UriUtil;
import org.jhotdraw8.fxbase.concurrent.SimpleWorkState;
import org.jhotdraw8.fxbase.concurrent.WorkState;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.NullableObjectKey;
import org.jhotdraw8.fxcollection.typesafekey.SimpleParameterizedType;
import org.jhotdraw8.icollection.ChampMap;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.concurrent.CancellationException;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Saves the changes in the active view. If the active view has not an URI, an
 * {@code URIChooser} is presented.
 *
 */
public abstract class AbstractSaveFileAction extends AbstractActivityAction<FileBasedActivity> {

    private final boolean saveAs;
    private @Nullable Node oldFocusOwner;
    public static final Key<URIChooser> SAVE_CHOOSER_KEY = new NullableObjectKey<>("saveChooser", URIChooser.class);
    public static final Key<Supplier<URIChooser>> SAVE_CHOOSER_FACTORY_KEY = new NullableObjectKey<>("saveChooserFactory",
            new SimpleParameterizedType(Supplier.class, URIChooser.class));

    /**
     * Creates a new instance.
     *
     * @param activity the view
     * @param id       the id
     * @param saveAs   whether to force a file dialog
     */
    public AbstractSaveFileAction(FileBasedActivity activity, String id, boolean saveAs) {
        this(activity, id, saveAs, activity.getApplication().getResources());
    }

    /**
     * Creates a new instance.
     *
     * @param activity  the view
     * @param id        the id
     * @param saveAs    whether to force a file dialog
     * @param resources the resources are used for setting labels and icons for the action
     */
    public AbstractSaveFileAction(FileBasedActivity activity, String id, boolean saveAs, Resources resources) {
        super(activity);
        this.saveAs = saveAs;
        resources.configureAction(this, id);
    }

    protected URIChooser getChooser(FileBasedActivity view) {
        URIChooser chooser = app.get(SAVE_CHOOSER_KEY);
        if (chooser == null) {
            Supplier<URIChooser> factory = app.get(SAVE_CHOOSER_FACTORY_KEY);
            chooser = factory == null ? new FileURIChooser(FileURIChooser.Mode.SAVE) : factory.get();
            app.set(SAVE_CHOOSER_KEY, chooser);
        }
        return chooser;
    }


    @Override
    protected void onActionPerformed(ActionEvent evt, FileBasedActivity activity) {
        oldFocusOwner = activity.getNode().getScene().getFocusOwner();
        WorkState<Void> workState = new SimpleWorkState<>(getLabel());
        activity.addDisabler(workState);
        saveFileChooseUri(activity, workState);
    }

    protected void saveFileChooseUri(final FileBasedActivity v, WorkState<Void> workState) {
        if (v.getURI() == null || saveAs) {
            URIChooser chsr = getChooser(v);
            URI uri = chsr.showDialog(v.getNode());
            if (uri != null) {
                saveFileChooseOptions(v, uri, chsr.getDataFormat(), workState);
            } else {
                v.removeDisabler(workState);
            }
            if (oldFocusOwner != null) {
                oldFocusOwner.requestFocus();
            }
        } else {
            saveFileChooseOptions(v, v.getURI(), v.getDataFormat(), workState);
        }
    }

    protected void saveFileChooseOptions(final FileBasedActivity v, URI uri, DataFormat format, WorkState<Void> workState) {
        SequencedMap<Key<?>, Object> options = new LinkedHashMap<>();
        Dialog<SequencedMap<Key<?>, Object>> dialog = null;
        try {
            dialog = createOptionsDialog(format);
        } catch (RuntimeException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, createErrorMessage(e));
            alert.getDialogPane().setMaxWidth(640.0);
            Resources labels = ApplicationLabels.getResources();
            alert.setHeaderText(labels.getFormatted("file.save.couldntSave.message", UriUtil.getName(uri)));
            alert.showAndWait();
            v.removeDisabler(this);
            return;
        }
        if (dialog != null) {
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.initOwner(v.getNode().getScene().getWindow());
            Optional<SequencedMap<Key<?>, Object>> result = dialog.showAndWait();

            if (result.isPresent()) {
                options = result.get();
            } else {
                v.removeDisabler(workState);
                return;
            }
        }
        saveFileToUri(v, uri, format, options, workState);
    }

    protected void saveFileToUri(final FileBasedActivity view, final URI uri, final DataFormat format, Map<Key<?>, Object> options, WorkState<Void> workState) {
        view.write(uri, format, ChampMap.copyOf(options), workState).handle((result, exception) -> {
            if (exception instanceof CancellationException) {
                view.removeDisabler(workState);
                if (oldFocusOwner != null) {
                    oldFocusOwner.requestFocus();
                }
            } else if (exception != null) {
                Throwable value = exception;
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unexpected Exception " + value.getMessage(), value);

                Resources labels = ApplicationLabels.getResources();
                Alert alert = new Alert(Alert.AlertType.ERROR, createErrorMessage(exception));
                alert.getDialogPane().setMaxWidth(640.0);
                alert.setHeaderText(labels.getFormatted("file.save.couldntSave.message", UriUtil.getName(uri)));
                alert.showAndWait();
                view.removeDisabler(workState);
                if (oldFocusOwner != null) {
                    oldFocusOwner.requestFocus();
                }
            } else {
                onSaveSucceeded(view, uri, format);
                view.removeDisabler(workState);
                if (oldFocusOwner != null) {
                    oldFocusOwner.requestFocus();
                }
            }
            //noinspection ReturnOfNull
            return null;
        });
    }

    protected @Nullable Dialog<SequencedMap<Key<?>, Object>> createOptionsDialog(DataFormat format) {
        return null;
    }

    protected abstract void onSaveSucceeded(FileBasedActivity v, URI uri, DataFormat format);
}
