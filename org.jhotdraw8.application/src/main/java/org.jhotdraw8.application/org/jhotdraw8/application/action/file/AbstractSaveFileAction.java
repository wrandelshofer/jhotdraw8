/*
 * @(#)AbstractSaveFileAction.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action.file;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.input.DataFormat;
import javafx.stage.Modality;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.application.Activity;
import org.jhotdraw8.application.ApplicationLabels;
import org.jhotdraw8.application.FileBasedActivity;
import org.jhotdraw8.application.FileBasedApplication;
import org.jhotdraw8.application.action.AbstractActivityAction;
import org.jhotdraw8.application.controls.urichooser.FileURIChooser;
import org.jhotdraw8.application.controls.urichooser.URIChooser;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.base.net.UriUtil;
import org.jhotdraw8.collection.champ.ChampMap;
import org.jhotdraw8.collection.reflect.TypeToken;
import org.jhotdraw8.fxbase.concurrent.SimpleWorkState;
import org.jhotdraw8.fxbase.concurrent.WorkState;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.SimpleNullableKey;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.function.Supplier;

/**
 * Saves the changes in the active view. If the active view has not an URI, an
 * {@code URIChooser} is presented.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractSaveFileAction extends AbstractActivityAction<FileBasedActivity> {

    private final boolean saveAs;
    private Node oldFocusOwner;
    public static final @NonNull Key<URIChooser> SAVE_CHOOSER_KEY = new SimpleNullableKey<>("saveChooser", URIChooser.class);
    public static final @NonNull Key<Supplier<URIChooser>> SAVE_CHOOSER_FACTORY_KEY = new SimpleNullableKey<>("saveChooserFactory",
            new TypeToken<Supplier<URIChooser>>() {
            });

    /**
     * Creates a new instance.
     *
     * @param activity the view
     * @param id       the id
     * @param saveAs   whether to force a file dialog
     */
    public AbstractSaveFileAction(@NonNull FileBasedActivity activity, String id, boolean saveAs) {
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
    public AbstractSaveFileAction(@NonNull FileBasedActivity activity, @NonNull String id, boolean saveAs, @NonNull Resources resources) {
        super(activity);
        this.saveAs = saveAs;
        resources.configureAction(this, id);
    }

    protected @NonNull URIChooser getChooser(FileBasedActivity view) {
        URIChooser chooser = app.get(SAVE_CHOOSER_KEY);
        if (chooser == null) {
            Supplier<URIChooser> factory = app.get(SAVE_CHOOSER_FACTORY_KEY);
            chooser = factory == null ? new FileURIChooser(FileURIChooser.Mode.SAVE) : factory.get();
            app.set(SAVE_CHOOSER_KEY, chooser);
        }
        return chooser;
    }


    @Override
    protected void onActionPerformed(ActionEvent evt, @Nullable FileBasedActivity activity) {
        if (activity == null) {
            return;
        }
        oldFocusOwner = activity.getNode().getScene().getFocusOwner();
        WorkState<Void> workState = new SimpleWorkState<>(getLabel());
        activity.addDisabler(workState);
        saveFileChooseUri(activity, workState);
    }

    protected void saveFileChooseUri(final @NonNull FileBasedActivity v, WorkState<Void> workState) {
        if (v.getURI() == null || saveAs) {
            URIChooser chsr = getChooser(v);
            //int option = fileChooser.showSaveDialog(this);

            URI uri;
            Outer:
            while (true) {
                uri = chsr.showDialog(v.getNode());

                // Prevent save to URI that is open in another view!
                // unless  multipe views to same URI are supported
                if (uri != null && !app.getNonNull(FileBasedApplication.ALLOW_MULTIPLE_ACTIVITIES_WITH_SAME_URI)) {
                    for (Activity pi : app.getActivities()) {
                        FileBasedActivity vi = (FileBasedActivity) pi;
                        if (vi != v && uri.equals(v.getURI())) {
                            // FIXME Localize message
                            Alert alert = new Alert(Alert.AlertType.INFORMATION, "You can not save to a file which is already open.");
                            alert.getDialogPane().setMaxWidth(640.0);
                            alert.showAndWait();
                            continue Outer;
                        }
                    }
                }
                break;
            }
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

    protected void saveFileChooseOptions(final @NonNull FileBasedActivity v, @NonNull URI uri, DataFormat format, WorkState<Void> workState) {
        Map<Key<?>, Object> options = new LinkedHashMap<>();
        Dialog<Map<Key<?>, Object>> dialog = null;
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
            Optional<Map<Key<?>, Object>> result = dialog.showAndWait();

            if (result.isPresent()) {
                options = result.get();
            } else {
                v.removeDisabler(workState);
                return;
            }
        }
        saveFileToUri(v, uri, format, options, workState);
    }

    protected void saveFileToUri(final @NonNull FileBasedActivity view, final @NonNull URI uri, final DataFormat format, @NonNull Map<Key<?>, Object> options, WorkState<Void> workState) {
        view.write(uri, format, ChampMap.copyOf(options), workState).handle((result, exception) -> {
            if (exception instanceof CancellationException) {
                view.removeDisabler(workState);
                if (oldFocusOwner != null) {
                    oldFocusOwner.requestFocus();
                }
            } else if (exception != null) {
                Throwable value = exception;
                value.printStackTrace();
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
            return null;
        });
    }

    protected @Nullable Dialog<Map<Key<?>, Object>> createOptionsDialog(DataFormat format) {
        return null;
    }

    protected abstract void onSaveSucceeded(FileBasedActivity v, URI uri, DataFormat format);
}
