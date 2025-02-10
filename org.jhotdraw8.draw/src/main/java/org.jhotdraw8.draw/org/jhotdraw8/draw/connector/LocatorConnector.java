/*
 * @(#)LocatorConnector.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.connector;

import javafx.geometry.Point2D;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.locator.Locator;
import org.jhotdraw8.geom.PointAndDerivative;

/**
 * LocatorConnector uses a {@link Locator} to compute its position.
 *
 */
public class LocatorConnector extends AbstractConnector {

    private final Locator locator;

    /**
     * Creates a new instance
     *
     * @param locator the locator that should be used
     */
    public LocatorConnector(Locator locator) {
        this.locator = locator;
    }

    /**
     * Returns the locator used to compute the position of the connector.
     *
     * @return the locator
     */
    public Locator getLocator() {
        return locator;
    }

    @Override
    public PointAndDerivative getPointAndDerivativeInLocal(Figure connection, Figure target) {
        return new PointAndDerivative(locator.locate(target).getX(), locator.locate(target).getY(), new Point2D(1, 0).getX(), new Point2D(1, 0).getY());
    }
}
