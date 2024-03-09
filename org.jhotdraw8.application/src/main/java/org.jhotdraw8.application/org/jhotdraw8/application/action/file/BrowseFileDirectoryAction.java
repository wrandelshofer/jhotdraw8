/*
 * @(#)BrowseFileDirectoryAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action.file;

import javafx.event.ActionEvent;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.application.FileBasedActivity;
import org.jhotdraw8.application.action.AbstractActivityAction;

import java.awt.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BrowseFileDirectoryAction extends AbstractActivityAction<FileBasedActivity> {

    public static final String ID = "file.browseFileDirectory";

    /**
     * Creates a new instance.
     *
     * @param activity the view
     */
    public BrowseFileDirectoryAction(@NonNull FileBasedActivity activity) {
        super(activity);
        activity.getApplication().getResources().configureAction(this, ID);
    }

    @Override
    protected void onActionPerformed(ActionEvent event, @NonNull FileBasedActivity activity) {
        if (isDisabled()) {
            return;
        }
        final URI uri = activity.getURI();
        doIt(uri);
    }

    private void doIt(@Nullable URI uri) {
        browseFileDirectory(uri);
    }

    public static void browseFileDirectory(@Nullable URI uri) {
        if (uri == null) {
            return;
        }
        try {
            Path path = Paths.get(uri);
            //Desktop.getDesktop().browseFileDirectory(path.toFile());
            try {
                try {
                    Desktop.class.getMethod("browseFileDirectory", File.class).invoke(Desktop.getDesktop(), path.toFile());
                } catch (IllegalAccessException | InvocationTargetException e) {
                    Logger.getLogger(BrowseFileDirectoryAction.class.getName()).log(Level.WARNING, "Unexpected Exception.", e);

                }
            } catch (NoSuchMethodException e) {
                Logger.getLogger(BrowseFileDirectoryAction.class.getName()).log(Level.WARNING, "Unexpected Exception.", e);

            }
        } catch (FileSystemNotFoundException e) {
            Logger.getLogger(BrowseFileDirectoryAction.class.getName()).warning(e.getMessage());
        }
    }

}
