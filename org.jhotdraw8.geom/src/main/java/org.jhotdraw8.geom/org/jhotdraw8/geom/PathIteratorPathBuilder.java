/*
 * @(#)PathIteratorPathBuilder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.geom;

import org.jhotdraw8.annotation.NonNull;

import java.awt.geom.PathIterator;

public class PathIteratorPathBuilder extends AbstractPathBuilder<PathIterator> {
    private int numCommands;
    private int numCoords;
    private byte @NonNull [] commands = new byte[10];
    private double @NonNull [] coords = new double[60];
    private final int windingRule;
    private boolean needsMoveTo = true;

    public PathIteratorPathBuilder() {
        this(PathIterator.WIND_EVEN_ODD);
    }

    public PathIteratorPathBuilder(int windingRule) {
        this.windingRule = windingRule;
    }

    public boolean isEmpty() {
        return numCoords == 0;
    }

    private void needRoom() {
        if (numCommands >= commands.length) {
            int newSize = commands.length + 10;
            if (newSize < commands.length) {
                throw new ArrayIndexOutOfBoundsException("Can't expand commands array.");
            }
            byte[] temp = commands;
            commands = new byte[newSize];
            System.arraycopy(temp, 0, commands, 0, temp.length);
        }
        if (numCoords + 6 >= coords.length) {
            int newSize = coords.length + 60;
            if (newSize < commands.length) {
                throw new ArrayIndexOutOfBoundsException("Can't expand coords array.");
            }
            double[] temp = coords;
            coords = new double[newSize];
            System.arraycopy(temp, 0, coords, 0, temp.length);
        }
    }

    @Override
    protected void doClosePath(double lastX, double lastY, double lastMoveToX, double lastMoveToY) {
        if (needsMoveTo) {
            return;
        }
        needRoom();
        commands[numCommands++] = PathIterator.SEG_CLOSE;
        needsMoveTo = true;
    }

    @Override
    protected void doCurveTo(double lastX, double lastY, double x1, double y1, double x2, double y2, double x, double y) {
        if (needsMoveTo) {
            doMoveTo(lastX, lastY);
        }
        needRoom();
        commands[numCommands++] = PathIterator.SEG_CUBICTO;
        coords[numCoords++] = x1;
        coords[numCoords++] = y1;
        coords[numCoords++] = x2;
        coords[numCoords++] = y2;
        coords[numCoords++] = x;
        coords[numCoords++] = y;
    }

    @Override
    protected void doLineTo(double lastX, double lastY, double x, double y) {
        if (needsMoveTo) {
            doMoveTo(lastX, lastY);
        }
        needRoom();
        commands[numCommands++] = PathIterator.SEG_LINETO;
        coords[numCoords++] = x;
        coords[numCoords++] = y;
    }

    @Override
    protected void doMoveTo(double x, double y) {
        needRoom();
        commands[numCommands++] = PathIterator.SEG_MOVETO;
        coords[numCoords++] = x;
        coords[numCoords++] = y;
        needsMoveTo = false;
    }

    @Override
    protected void doQuadTo(double lastX, double lastY, double x1, double y1, double x, double y) {
        if (needsMoveTo) {
            doMoveTo(lastX, lastY);
        }
        needRoom();
        commands[numCommands++] = PathIterator.SEG_QUADTO;
        coords[numCoords++] = x1;
        coords[numCoords++] = y1;
        coords[numCoords++] = x;
        coords[numCoords++] = y;
    }

    @Override
    public @NonNull PathIterator build() {
        return new MyPathIterator(windingRule, numCommands, numCoords, commands, coords);
    }

    private static class MyPathIterator implements PathIterator {
        private int commandIndex = 0;
        private int coordsIndex = 0;
        private final int numCommands;
        private final int numCoords;
        private final byte[] commands;
        private final double[] coords;
        private static final int[] curvecoords = {2, 2, 4, 6, 0};
        private final int windingRule;

        public MyPathIterator(int windingRule, int numCommands, int numCoords, byte[] commands, double[] coords) {
            this.windingRule = windingRule;
            this.numCommands = numCommands;
            this.numCoords = numCoords;
            this.commands = commands;
            this.coords = coords;
        }

        @Override
        public int getWindingRule() {
            return windingRule;
        }

        @Override
        public boolean isDone() {
            return commandIndex >= numCommands;
        }

        @Override
        public void next() {
            if (!isDone()) {
                int type = commands[commandIndex++];
                coordsIndex += curvecoords[type];
            }
        }

        @Override
        public int currentSegment(float[] coords) {
            int type = commands[commandIndex];
            for (int i = 0, n = curvecoords[type]; i < n; i++) {
                coords[i] = (float) this.coords[i + coordsIndex];
            }
            return type;
        }

        @Override
        public int currentSegment(double[] coords) {
            int type = commands[commandIndex];
            for (int i = 0, n = curvecoords[type]; i < n; i++) {
                coords[i] = this.coords[i + coordsIndex];
            }
            return type;
        }
    }

}
