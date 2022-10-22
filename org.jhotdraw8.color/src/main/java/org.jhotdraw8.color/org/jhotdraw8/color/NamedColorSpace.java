/* @(#)NamedColorSpace.java
 * Copyright © The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.color;

/**
 * Interface for {@code ColorSpace} classes which have a name.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public interface NamedColorSpace {

    String getName();

    /**
     * Faster toRGB method which uses the provided output array.
     */
    float[] toRGB(float[] colorvalue, float[] rgb);

    /**
     * Faster fromRGB method which uses the provided output array.
     */
    float[] fromRGB(float[] rgb, float[] colorvalue);

    /**
     * Faster toCIEXYZ method which uses the provided output array.
     */
    float[] toCIEXYZ(float[] colorvalue, float[] xyz);

    /**
     * Faster fromCIEXYZ method which uses the provided output array.
     */
    float[] fromCIEXYZ(float[] xyz, float[] colorvalue);

}
