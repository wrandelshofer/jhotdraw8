/*
 * @(#)RectangleTransition.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.transition;

import javafx.animation.Transition;
import javafx.geometry.Bounds;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * RectangleTransition performs a linear transition of the bounds of a JavaFX Rectangle.
 *
 * @author Werner Randelshofer
 */
public class RectangleTransition extends Transition {
    private final Rectangle rectangle;
    private final Bounds fromBounds;
    private final Bounds toBounds;


    @SuppressWarnings("this-escape")
    public RectangleTransition(Duration duration, Rectangle rectangle, Bounds fromBounds, Bounds toBounds) {
        this.rectangle = rectangle;
        this.fromBounds = fromBounds;
        this.toBounds = toBounds;
        setCycleDuration(duration);
    }

    public Bounds getFromBounds() {
        return fromBounds;
    }

    public Bounds getToBounds() {
        return toBounds;
    }

    @Override
    protected void interpolate(double frac) {
        double invFrac = 1 - frac;
        rectangle.setWidth(fromBounds.getWidth() * invFrac + toBounds.getWidth() * frac);
        rectangle.setHeight(fromBounds.getHeight() * invFrac + toBounds.getHeight() * frac);
        rectangle.setX(fromBounds.getMinX() * invFrac + toBounds.getMinX() * frac);
        rectangle.setY(fromBounds.getMinY() * invFrac + toBounds.getMinY() * frac);
    }

}
