/*
 * @(#)OpenRecentFileAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action.file;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.DataFormat;
import org.jhotdraw8.application.Activity;
import org.jhotdraw8.application.Application;
import org.jhotdraw8.application.ApplicationLabels;
import org.jhotdraw8.application.FileBasedActivity;
import org.jhotdraw8.application.action.AbstractApplicationAction;
import org.jhotdraw8.application.action.Action;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.base.net.UriUtil;
import org.jhotdraw8.fxbase.concurrent.SimpleWorkState;
import org.jhotdraw8.fxbase.concurrent.WorkState;
import org.jhotdraw8.icollection.ChampMap;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loads the specified URI into an empty view. If no empty view is available, a
 * new view is created.
 * <p>
 * This action is called when the user selects an item in the Recent Files
 * submenu of the File menu. The action and the menu item is automatically
 * created by the application, when the {@code ApplicationModel} provides a
 * {@code OpenFileAction}.
 * <hr>
 * <b>Features</b>
 *
 * <p>
 * <em>Allow multiple views per URI</em><br>
 * When the feature is disabled, {@code OpenRecentFileAction} prevents opening
 * an URI which is opened in another view.<br>
 * See {@link org.jhotdraw8.application} for a description of the feature.
 * </p>
 *
 * <p>
 * <em>Open last URI on launch</em><br> {@code OpenRecentFileAction} supplies
 * data for this feature by calling
 * {@link Application#getRecentUris()}{@code .add()} when it
 * successfully opened a file. See {@link org.jhotdraw8.application} for a description
 * of the feature.
 * </p>
 *
 * @author Werner Randelshofer.
 */
public class OpenRecentFileAction extends AbstractApplicationAction {

    public static final String ID = "file.openRecent";
    private final URI uri;
    private final @Nullable DataFormat format;
    private final boolean reuseEmptyViews = true;

    /**
     * Creates a new instance.
     *
     * @param app    the application
     * @param uri    the uri
     * @param format the data format that should be used to access the URI
     */
    @SuppressWarnings("this-escape")
    public OpenRecentFileAction(Application app, URI uri, @Nullable DataFormat format) {
        super(app);
        this.uri = uri;
        this.format = format;
        set(Action.LABEL, UriUtil.getName(uri));
    }

    @Override
    protected void onActionPerformed(ActionEvent evt, Application app) {
        {
            // Check if there is already an activity with this URI.
            for (Activity activity : app.getActivities()) {
                FileBasedActivity fba = (FileBasedActivity) activity;
                if (Objects.equals(uri, fba.getURI())) {
                    fba.getNode().getScene().getWindow().requestFocus();
                    return;
                }
            }


            // Search for an empty view
            FileBasedActivity emptyView;
            if (reuseEmptyViews) {
                emptyView = (FileBasedActivity) app.getActiveActivity();//FIXME class cast exception
                if (emptyView == null
                        || !emptyView.isEmpty()
                        || emptyView.isDisabled()) {
                    emptyView = null;
                }
            } else {
                emptyView = null;
            }

            if (emptyView == null) {
                app.createActivity().thenAccept(v -> {
                    app.getActivities().add(v);
                    doIt((FileBasedActivity) v, true);
                });
            } else {
                doIt(emptyView, false);
            }
        }
    }

    public void doIt(FileBasedActivity view, boolean disposeView) {
        openViewFromURI(view, uri, format);
    }

    private void onException(final FileBasedActivity v, Throwable exception) throws MissingResourceException {
        Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unexpected Exception " + exception.getMessage(), exception);

        Resources labels = ApplicationLabels.getResources();
        Alert alert = new Alert(Alert.AlertType.ERROR, createErrorMessage(exception));
        alert.getDialogPane().setMaxWidth(640.0);
        alert.setHeaderText(labels.getFormatted("file.open.couldntOpen.message", UriUtil.getName(uri)));
        ButtonType removeUri = new ButtonType(labels.getString("file.removeOpenRecentEntry.buttonText"));
        alert.getButtonTypes().add(removeUri);
        // Note: we must invoke clear() or read() on the view, before we start using it.
        v.clear();
        Optional<ButtonType> selection = alert.showAndWait();
        if (selection.isPresent() && selection.get() == removeUri) {
            getApplication().getRecentUris().remove(uri);
        }
    }

    protected void openViewFromURI(final FileBasedActivity v, final URI uri, @Nullable DataFormat format) {
        final Application app = getApplication();
        WorkState<Void> workState = new SimpleWorkState<>(getLabel());
        v.addDisabler(workState);
        URI oldUri = v.getURI();

        v.setURI(uri); // tentatively set new URI so that other actions will not reuse this activity,
        // nor other actions will create a new activity with this URI

        // Open the file
        try {
            v.read(uri, format, ChampMap.of(), false, workState).whenComplete((actualFormat, exception) -> {
                if (exception instanceof CancellationException) {
                    v.removeDisabler(workState);
                    v.setURI(oldUri);
                } else if (exception != null) {
                    v.removeDisabler(workState);
                    v.setURI(oldUri);
                    onException(v, exception);
                } else {
                    v.setURI(uri);
                    v.setDataFormat(actualFormat);
                    v.clearModified();
                    v.removeDisabler(workState);
                }
                URI finalUri = v.getURI();
            });
        } catch (Throwable t) {
            v.removeDisabler(workState);
            onException(v, t);
        }
    }
}
