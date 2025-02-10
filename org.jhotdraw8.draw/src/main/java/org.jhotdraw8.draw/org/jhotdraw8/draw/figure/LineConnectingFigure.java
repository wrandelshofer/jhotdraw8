/*
 * @(#)LineConnectingFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.figure;

import javafx.geometry.Point2D;
import org.jhotdraw8.draw.connector.Connector;
import org.jhotdraw8.draw.key.CssPoint2DStyleableMapAccessor;
import org.jhotdraw8.draw.key.CssSizeStyleableKey;
import org.jhotdraw8.fxcollection.typesafekey.NullableObjectKey;

/**
 * LineConnectingFigure.
 *
 */
public interface LineConnectingFigure extends ConnectingFigure {
    /**
     * The end position of the line.
     */
    CssPoint2DStyleableMapAccessor END = LineFigure.END;
    /**
     * The end connector.
     */
    NullableObjectKey<Connector> END_CONNECTOR = new NullableObjectKey<>("endConnector", Connector.class, null);
    /**
     * The end target.
     * <p>
     * This property can not be styled with CSS because it affects the
     * layout observer relationship between the label and the target
     * figure.
     */
    NullableObjectKey<Figure> END_TARGET = new NullableObjectKey<>("endTarget", Figure.class, null);
    /**
     * The end position of the line.
     */
    CssSizeStyleableKey END_X = LineFigure.END_X;
    /**
     * The end position of the line.
     */
    CssSizeStyleableKey END_Y = LineFigure.END_Y;
    /**
     * The start position of the line.
     */
    CssPoint2DStyleableMapAccessor START = LineFigure.START;
    /**
     * The start connector.
     */
    NullableObjectKey<Connector> START_CONNECTOR = new NullableObjectKey<>("startConnector", Connector.class, null);
    /**
     * The start target.
     * <p>
     * This property can not be styled with CSS because it affects the
     * layout observer relationship between the label and the target
     * figure.
     */
    NullableObjectKey<Figure> START_TARGET = new NullableObjectKey<>("startTarget", Figure.class, null);
    /**
     * The start position of the line.
     */
    CssSizeStyleableKey START_X = LineFigure.START_X;
    /**
     * The start position of the line.
     */
    CssSizeStyleableKey START_Y = LineFigure.START_Y;

    default boolean isStartConnected() {
        return get(START_CONNECTOR) != null && get(START_TARGET) != null;
    }

    default boolean isEndConnected() {
        return get(END_CONNECTOR) != null && get(END_TARGET) != null;
    }

    /**
     * Gets the start target point or the start point if the start target point is not present.
     *
     * @return start target point or start point.
     */
    default Point2D getStartTargetPoint() {
        Connector connector = get(START_CONNECTOR);
        Figure target = get(START_TARGET);
        if (connector != null && target != null) {
            return worldToLocal(connector.getPointAndDerivativeInWorld(this, target).getPoint(Point2D::new));
        } else {
            return getNonNull(START).getConvertedValue();
        }
    }

    /**
     * Gets the end target point or the end point if the end target point is not present.
     *
     * @return end target point or end point.
     */
    default Point2D getEndTargetPoint() {
        Connector connector = get(END_CONNECTOR);
        Figure target = get(END_TARGET);
        if (connector != null && target != null) {
            return worldToLocal(connector.getPointAndDerivativeInWorld(this, target).getPoint(Point2D::new));
        } else {
            return getNonNull(END).getConvertedValue();
        }
    }
}
