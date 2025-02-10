/*
 * @(#)ResizePane.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcontrols.dock;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Cursor;
import javafx.scene.layout.BorderPane;

/**
 * ResizePane.
 *
 */
public class ResizePane extends BorderPane {

    private final BooleanProperty userResizable = new SimpleBooleanProperty(true);

    private final ResizeButton rb = new ResizeButton();

    public ResizePane() {
        rb.setTarget(this);
        setBottom(rb);
        rb.visibleProperty().bind(userResizable);
    }

    public void setResizeAxis(TrackAxis axis) {
        switch (axis) {
        case X:
            rb.setCursor(Cursor.H_RESIZE);
            setBottom(null);
            setRight(rb);
            break;
        case Y:
        case Z:
        default:
            rb.setCursor(Cursor.V_RESIZE);
            setRight(null);
            setBottom(rb);
            break;
        }
    }

    public boolean isUserResizable() {
        return userResizable.get();
    }

    public void setUserResizable(boolean value) {
        userResizable.set(value);
    }

    public BooleanProperty userResizableProperty() {
        return userResizable;
    }
}
