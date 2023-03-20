/*
 * @(#)XmlPoint2DConverter.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.xml.converter;

import org.jhotdraw8.draw.css.converter.Point2DConverter;

/**
 * Converts a {@code javafx.geometry.Point2D} into a {@code String} and vice
 * versa.
 *
 * @author Werner Randelshofer
 */
public class XmlPoint2DConverter extends Point2DConverter {


    public XmlPoint2DConverter() {
        this(false, true);
    }

    public XmlPoint2DConverter(boolean nullable) {
        this(nullable, true);
    }

    public XmlPoint2DConverter(boolean nullable, boolean withSpace) {
        super(nullable, withSpace);
    }
}
