/*
 * @(#)XmlNumberConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.xml.converter;

import org.jhotdraw8.base.converter.NumberConverter;

/**
 * Converts a {@code Double} into the XML String representation.
 * <p>
 * Reference:
 * <a href="http://www.w3.org/TR/2004/REC-xmlschema-2-20041028/#double">W3C: XML
 * Schema Part 2: Datatypes Second Edition: 3.2.5 double</a>
 * </p>
 *
 * @author Werner Randelshofer
 */
public class NumberXmlConverter extends NumberConverter {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance.
     */
    public NumberXmlConverter() {
    }

    /**
     * Creates a new instance.
     */
    public NumberXmlConverter(Class<? extends Number> clazz) {
        super(clazz);
    }

    /**
     * Creates a new instance that only accepts values in the specified range.
     *
     * @param min        the minimal accepted value (inclusive)
     * @param max        the maximal accepted value (inclusive)
     * @param multiplier a multiplication factor applied to the number after parsing it
     */
    public NumberXmlConverter(double min, double max, double multiplier) {
        super(min, max, multiplier);
    }

    public NumberXmlConverter(double min, double max, double multiplier, boolean allowsNullValue) {
        super(min, max, multiplier, allowsNullValue);
    }

    public NumberXmlConverter(double min, double max, double multiplier, boolean allowsNullValue, String unit) {
        super(min, max, multiplier, allowsNullValue, unit);
    }

}
