/*
 * @(#)AbstractLocator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.locator;

import javafx.geometry.Point2D;
import org.jhotdraw8.draw.figure.Figure;

/**
 * This abstract class can be extended to implement a {@link Locator}.
 *
 */
public abstract class AbstractLocator implements Locator {


    /**
     * Creates a new instance.
     */
    public AbstractLocator() {
    }

    @Override
    public Point2D locate(Figure owner, Figure dependent) {
        return locate(owner);
    }

}
