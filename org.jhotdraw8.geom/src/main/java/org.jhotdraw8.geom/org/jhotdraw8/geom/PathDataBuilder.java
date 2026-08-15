/*
 * @(#)PathMetricsBuilder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.geom;

public class PathDataBuilder extends AbstractPathDataBuilder<PathData> {
    public PathDataBuilder() {
    }

    @Override
    public PathData build() {
        return new PathData(commands.toByteArray(), offsets.toIntArray(), coords.toDoubleArray(), windingRule);
    }
}
