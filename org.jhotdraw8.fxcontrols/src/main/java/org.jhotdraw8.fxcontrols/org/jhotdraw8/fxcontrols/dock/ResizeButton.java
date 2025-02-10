/*
 * @(#)ResizeButton.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcontrols.dock;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

/**
 * ResizeButton.
 *
 */
public class ResizeButton extends Region {

    private Point2D pressed;
    private Point2D size;
    private final ObjectProperty<Region> target = new SimpleObjectProperty<>();

    public ResizeButton() {
        setOnMousePressed(this::mousePressed);
        setOnMouseDragged(this::mouseDragged);
        setCursor(Cursor.V_RESIZE);
        setMinHeight(1);
        setMinWidth(1);
    }

    public Region getTarget() {
        return target.get();
    }

    public void setTarget(Region value) {
        target.set(value);
    }

    private void mouseDragged(MouseEvent evt) {
        final Region t = getTarget();
        if (t != null && pressed != null) {
            Point2D current = new Point2D(evt.getSceneX(), evt.getSceneY());
            Point2D delta = current.subtract(pressed);
            t.setPrefWidth(size.getX() + delta.getX());
            t.setPrefHeight(size.getY() + delta.getY());
        }
    }

    private void mousePressed(MouseEvent evt) {
        final Region t = getTarget();
        if (t != null) {
            pressed = new Point2D(evt.getSceneX(), evt.getSceneY());
            size = new Point2D(getTarget().getWidth(), t.getHeight());
        }
    }

    public ObjectProperty<Region> targetProperty() {
        return target;
    }

}
