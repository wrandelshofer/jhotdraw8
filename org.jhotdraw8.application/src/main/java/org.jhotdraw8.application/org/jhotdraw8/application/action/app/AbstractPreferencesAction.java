/*
 * @(#)AbstractPreferencesAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action.app;

import org.jhotdraw8.application.Application;
import org.jhotdraw8.application.ApplicationLabels;
import org.jhotdraw8.application.action.AbstractApplicationAction;

/**
 * Displays a preferences dialog for the application.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractPreferencesAction extends AbstractApplicationAction {

    public static final String ID = "application.preferences";

    /**
     * Creates a new instance.
     *
     * @param app the application
     */
    @SuppressWarnings("this-escape")
    public AbstractPreferencesAction(Application app) {
        super(app);
        ApplicationLabels.getResources().configureAction(this, ID);
    }
}
