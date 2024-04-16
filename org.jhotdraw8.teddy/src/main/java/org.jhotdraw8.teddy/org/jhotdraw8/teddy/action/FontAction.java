/*
 * @(#)FontAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.teddy.action;

import javafx.event.ActionEvent;
import javafx.scene.text.Font;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.application.Application;
import org.jhotdraw8.application.action.AbstractActivityAction;
import org.jhotdraw8.fxcontrols.fontchooser.FontDialog;
import org.jhotdraw8.fxcontrols.fontchooser.FontFamilySize;
import org.jhotdraw8.teddy.TeddyLabels;

import java.util.Optional;

public class FontAction extends AbstractActivityAction<FontableActivity> {

    private FontDialog fontDialog;
    public static final @NonNull String ID = "format.font";

    /**
     * Creates a new instance which acts on the specified activity of the
     * application.
     *
     * @param app      The application.
     * @param activity The activity. If activity is null then the action acts on
     *                 the active activity of the application. Otherwise it will act on the
     *                 specified activity.
     */
    public FontAction(@NonNull Application app, @Nullable FontableActivity activity) {
        super(activity);
        TeddyLabels.getResources().configureAction(this, ID);
    }

    @Override
    protected void onActionPerformed(@NonNull ActionEvent event, @NonNull FontableActivity activity) {
        if (fontDialog == null) {
            fontDialog = new FontDialog();
            fontDialog.initOwner(activity.getNode().getScene().getWindow());
        }
        FontableActivity foa = activity;
        Optional<FontFamilySize> fontFamilySize = fontDialog.showAndWait(
                new FontFamilySize(foa.getFont().getFamily(), foa.getFont().getSize()));
        fontFamilySize.ifPresent(f -> foa.setFont(Font.font(f.family(), f.size())));
    }
}
