/*
 * @(#)XmlNumberConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.converter;

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
public class XmlNumberConverter extends NumberConverter {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance.
     */
    public XmlNumberConverter() {
    }

    /**
     * Creates a new instance that only accepts values in the specified range.
     *
     * @param min        the minimal accepted value (inclusive)
     * @param max        the maximal accepted value (inclusive)
     * @param multiplier a multiplication factor applied to the number after parsing it
     */
    public XmlNumberConverter(double min, double max, double multiplier) {
        super(min, max, multiplier);
    }

    public XmlNumberConverter(double min, double max, double multiplier, boolean allowsNullValue) {
        super(min, max, multiplier, allowsNullValue);
    }

    public XmlNumberConverter(double min, double max, double multiplier, boolean allowsNullValue, String unit) {
        super(min, max, multiplier, allowsNullValue, unit);
    }

}
