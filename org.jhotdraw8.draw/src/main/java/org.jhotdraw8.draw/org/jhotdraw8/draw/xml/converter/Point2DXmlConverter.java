/*
 * @(#)XmlPoint2DConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.xml.converter;

import org.jhotdraw8.draw.css.converter.Point2DConverter;

/**
 * Converts a {@code javafx.geometry.Point2D} into a {@code String} and vice
 * versa.
 *
 * @author Werner Randelshofer
 */
public class Point2DXmlConverter extends Point2DConverter {


    public Point2DXmlConverter() {
        this(false, true);
    }

    public Point2DXmlConverter(boolean nullable) {
        this(nullable, true);
    }

    public Point2DXmlConverter(boolean nullable, boolean withSpace) {
        super(nullable, withSpace);
    }
}
