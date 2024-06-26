/*
 * @(#)PlineOffsetSegment.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom.contour;


import java.awt.geom.Point2D;

/**
 * PlineOffsetSegment.
 * <p>
 * References:
 * <p>
 * This code has been derived from CavalierContours.
 * <dl>
 *     <dt>CavalierContours. Copyright (c) 2019 Jedidiah Buck McCready.
 *    <a href="https://github.com/jbuckmccready/CavalierContours/blob/7a35376eb4c2d5f917d3e0564ea630c94137255e/LICENSE">MIT License.</a></dt>
 *     <dd><a href="https://github.com/jbuckmccready/CavalierContours">github.com</a></dd>
 * </dl>
 */
record PlineOffsetSegment(PlineVertex v1, PlineVertex v2, Point2D.Double origV2Pos,
                          boolean collapsedArc) {


}
