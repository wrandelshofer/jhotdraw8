/*
 * @(#)NullConstrainer.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.constrain;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.shape.Path;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.css.value.CssRectangle2D;
import org.jhotdraw8.draw.figure.Figure;

/**
 * NullConstrainer does not constrain anything.
 *
 */
public class NullConstrainer extends AbstractConstrainer {

    private final Path node = new Path();

    public NullConstrainer() {
    }

    @Override
    public CssPoint2D translatePoint(Figure f, CssPoint2D cssp, CssPoint2D cssdir) {
        Point2D p = cssp.getConvertedValue();
        Point2D dir = cssdir.getConvertedValue();
        return new CssPoint2D(p.add(dir));
    }

    @Override
    public CssRectangle2D translateRectangle(Figure f, CssRectangle2D cssr, CssPoint2D cssdir) {
        Rectangle2D r = cssr.getConvertedValue();
        Point2D dir = cssdir.getConvertedValue();
        return new CssRectangle2D(r.getMinX() + dir.getX(), r.getMinY() + dir.getY(), r.getWidth(), r.getHeight());
    }

    @Override
    public double translateAngle(Figure f, double angle, double dir) {
        return angle + dir;
    }

    @Override
    public Node getNode() {
        return node;
    }

    @Override
    public void updateNode(DrawingView drawingView) {
        // empty
    }

}
