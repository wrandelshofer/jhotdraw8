package org.jhotdraw8.geom;

public record PathData(byte[] commands, int[] offsets, double[] coords, int windingRule) {
}
