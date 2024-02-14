/*
 * @(#)XmlRectangle2DConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.xml.converter;

import org.jhotdraw8.draw.css.converter.Rectangle2DConverter;

/**
 * Converts a {@code javafx.geometry.Rectangle2D} into a {@code String} and vice
 * versa.
 *
 * @author Werner Randelshofer
 */
public class Rectangle2DXmlConverter extends Rectangle2DConverter {

    public Rectangle2DXmlConverter() {
        this(false, true);
    }

    public Rectangle2DXmlConverter(boolean nullable) {
        this(nullable, true);
    }

    public Rectangle2DXmlConverter(boolean nullable, boolean withSpace) {
        super(nullable, withSpace, false);
    }
}
