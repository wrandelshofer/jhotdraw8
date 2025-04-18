/*
 * @(#)AbstractOpenFileAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action.file;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.input.DataFormat;
import org.jhotdraw8.application.Activity;
import org.jhotdraw8.application.Application;
import org.jhotdraw8.application.ApplicationLabels;
import org.jhotdraw8.application.FileBasedActivity;
import org.jhotdraw8.application.FileBasedApplication;
import org.jhotdraw8.application.action.AbstractApplicationAction;
import org.jhotdraw8.application.controls.urichooser.FileURIChooser;
import org.jhotdraw8.application.controls.urichooser.URIChooser;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.base.net.UriUtil;
import org.jhotdraw8.fxbase.concurrent.SimpleWorkState;
import org.jhotdraw8.fxbase.concurrent.WorkState;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.SimpleNullableKey;
import org.jhotdraw8.fxcollection.typesafekey.SimpleParameterizedType;
import org.jhotdraw8.icollection.ChampMap;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class AbstractOpenFileAction extends AbstractApplicationAction {
    public static final Key<URIChooser> OPEN_CHOOSER_KEY = new SimpleNullableKey<>("openChooser", URIChooser.class);
    public static final Key<Supplier<URIChooser>> OPEN_CHOOSER_FACTORY_KEY = new SimpleNullableKey<>("openChooserFactory",
            new SimpleParameterizedType(Supplier.class, URIChooser.class));

    public AbstractOpenFileAction(FileBasedApplication app) {
        super(app);
    }

    protected @Nullable URIChooser getChooser(FileBasedActivity view) {
        URIChooser chooser = app.get(OPEN_CHOOSER_KEY);
        if (chooser == null) {
            Supplier<URIChooser> factory = app.get(OPEN_CHOOSER_FACTORY_KEY);
            chooser = factory == null ? new FileURIChooser() : factory.get();
            app.set(OPEN_CHOOSER_KEY, chooser);
        }
        return chooser;
    }

    protected abstract boolean isReuseEmptyViews();

    @Override
    protected void onActionPerformed(ActionEvent evt, Application app) {
        {
            WorkState<Void> workState = new SimpleWorkState<>(getLabel());
            app.addDisabler(workState);
            // Search for an empty view
            FileBasedActivity emptyView;
            if (isReuseEmptyViews()) {
                emptyView = (FileBasedActivity) app.getActiveActivity(); // FIXME class cast exception
                if (emptyView == null
                        || !emptyView.isEmpty()
                        || emptyView.isDisabled()) {
                    emptyView = null;
                }
            } else {
                emptyView = null;
            }

            if (emptyView == null) {
                app.createActivity().thenAccept(v -> doIt((FileBasedActivity) v, true, workState));
            } else {
                doIt(emptyView, false, workState);
            }
        }
    }


    public void doIt(FileBasedActivity view, boolean disposeView, WorkState<Void> workState) {
        URIChooser chooser = getChooser(view);
        URI uri = chooser.showDialog(app.getNode());
        if (uri != null) {
            app.getActivities().add(view);

            // Prevent same URI from being opened more than once
            if (!getApplication().getNonNull(FileBasedApplication.ALLOW_MULTIPLE_ACTIVITIES_WITH_SAME_URI)) {
                for (Activity vp : getApplication().getActivities()) {
                    FileBasedActivity v = (FileBasedActivity) vp;
                    if (v.getURI() != null && v.getURI().equals(uri)) {
                        if (disposeView) {
                            app.getActivities().remove(view);
                        }
                        app.removeDisabler(workState);
                        v.getNode().getScene().getWindow().requestFocus();
                        v.getNode().requestFocus();
                        return;
                    }
                }
            }

            openActivityFromURI(view, uri, chooser, workState);
        } else {
            if (disposeView) {
                app.getActivities().remove(view);
            }
            app.removeDisabler(workState);
        }
    }

    protected void openActivityFromURI(final FileBasedActivity v, final URI uri, final URIChooser chooser, WorkState<Void> workState) {
        final Application app = getApplication();
        Map<Key<?>, Object> options = getReadOptions();
        app.removeDisabler(workState);

        v.addDisabler(workState);
        final DataFormat chosenFormat = chooser.getDataFormat();
        v.setDataFormat(chosenFormat);

        // Open the file
        v.read(uri, chosenFormat, ChampMap.copyOf(options), false, workState).whenComplete((actualFormat, exception) -> {
            if (exception instanceof CancellationException) {
                v.removeDisabler(workState);
            } else if (exception != null) {
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unexpected Exception " + exception.getMessage(), exception);

                Resources labels = ApplicationLabels.getResources();

                TextArea textArea = new TextArea(createErrorMessage(exception));
                textArea.setEditable(false);
                textArea.setWrapText(true);

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.getDialogPane().setMaxWidth(640.0);
                alert.setHeaderText(labels.getFormatted("file.open.couldntOpen.message", UriUtil.getName(uri)));
                alert.getDialogPane().setContent(textArea);
                alert.showAndWait();
                v.removeDisabler(workState);
            } else {
                v.setURI(uri);
                v.setDataFormat(actualFormat);
                v.clearModified();
                getApplication().getRecentUris().put(uri, actualFormat);
                v.removeDisabler(workState);
            }
        });
    }

    /**
     * Gets options for {@link FileBasedActivity#read}.
     * The options can be null, a constant, or from user input through a dialog window.
     * <p>
     * The value null means that the user has aborted the dialog window. In this case, the action
     * will not open a file!
     *
     * @return options or null if the user has aborted the dialog window
     */
    protected abstract Map<Key<?>, Object> getReadOptions();


}
