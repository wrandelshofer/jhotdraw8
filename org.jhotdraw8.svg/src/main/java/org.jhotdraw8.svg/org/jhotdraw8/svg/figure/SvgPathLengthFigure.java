/*
 * @(#)SvgPathLengthFigure.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.svg.figure;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.key.NullableDoubleStyleableKey;

public interface SvgPathLengthFigure extends Figure {
    /**
     * pathLength.
     * <a href="https://www.w3.org/TR/2018/CR-SVG2-20181004/paths.html#PathLengthAttribute">link</a>.
     */
    @NonNull NullableDoubleStyleableKey PATH_LENGTH = new NullableDoubleStyleableKey("pathLength", null);

}
