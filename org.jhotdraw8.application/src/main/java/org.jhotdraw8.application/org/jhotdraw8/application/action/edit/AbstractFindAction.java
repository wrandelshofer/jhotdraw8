/*
 * @(#)AbstractFindAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action.edit;

import org.jhotdraw8.application.Activity;
import org.jhotdraw8.application.Application;
import org.jhotdraw8.application.ApplicationLabels;
import org.jhotdraw8.application.action.AbstractActivityAction;

/**
 * Presents a find dialog to the user and then highlights the found items in the
 * active view.
 *
 * @param <A> the activity type
 * @author Werner Randelshofer
 */
public abstract class AbstractFindAction<A extends Activity> extends AbstractActivityAction<A> {

    public static final String ID = "edit.find";

    /**
     * Creates a new instance.
     *
     * @param app       the application
     * @param view      the view
     * @param viewClass the class of the view
     */
    @SuppressWarnings("this-escape")
    public AbstractFindAction(Application app, A view, Class<A> viewClass) {
        super(view);
        ApplicationLabels.getResources().configureAction(this, ID);
    }
}
