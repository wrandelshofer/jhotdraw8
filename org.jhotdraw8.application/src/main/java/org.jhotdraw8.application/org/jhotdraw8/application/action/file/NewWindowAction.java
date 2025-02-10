/*
 * @(#)NewWindowAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action.file;

import org.jhotdraw8.application.Application;

/**
 * Creates a new view.
 *
 */
public class NewWindowAction extends NewFileAction {

    public static final String ID = "file.newWindow";

    /**
     * Creates a new instance.
     *
     * @param app the application
     */
    public NewWindowAction(Application app) {
        super(app, ID);
    }
}
