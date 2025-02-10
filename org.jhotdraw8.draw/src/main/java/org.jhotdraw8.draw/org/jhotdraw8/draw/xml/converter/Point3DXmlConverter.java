/*
 * @(#)XmlPoint3DConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.xml.converter;

import org.jhotdraw8.draw.css.converter.Point3DConverter;

/**
 * Converts a {@code javafx.geometry.Point3D} into a {@code String} and vice
 * versa.
 *
 */
public class Point3DXmlConverter extends Point3DConverter {

    public Point3DXmlConverter() {
        this(false, true);
    }

    public Point3DXmlConverter(boolean nullable) {
        this(nullable, true);
    }

    public Point3DXmlConverter(boolean nullable, boolean withSpace) {
        super(nullable, withSpace);
    }
}
