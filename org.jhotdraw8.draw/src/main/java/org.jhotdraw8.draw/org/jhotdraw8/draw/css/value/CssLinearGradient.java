/*
 * @(#)CssLinearGradient.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.value;

import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.DefaultUnitConverter;
import org.jhotdraw8.css.value.UnitConverter;
import org.jhotdraw8.draw.css.converter.CssStop;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a linear gradient.
 *
 * @author Werner Randelshofer
 */
public class CssLinearGradient implements Paintable {

    private LinearGradient linearGradient;
    private final double startX;
    private final double startY;
    private final double endX;
    private final double endY;
    private final boolean proportional;
    private final CycleMethod cycleMethod;
    private final CssStop[] cstops;

    public CssLinearGradient(double startX, double startY, double endX, double endY, boolean proportional,
                             CycleMethod cycleMethod,
                             CssStop... stops) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.proportional = proportional;
        this.cycleMethod = cycleMethod;
        this.cstops = stops;
    }

    public CssLinearGradient(double startX, double startY, double endX, double endY, boolean proportional, CycleMethod cycleMethod,
                             Collection<CssStop> stops) {
        this(startX, startY, endX, endY, proportional, cycleMethod, stops.toArray(new CssStop[0]));
    }

    public CssLinearGradient(LinearGradient linearGradient) {
        this.linearGradient = linearGradient;
        this.startX = linearGradient.getStartX();
        this.startY = linearGradient.getStartY();
        this.endX = linearGradient.getEndX();
        this.endY = linearGradient.getEndY();
        this.proportional = linearGradient.isProportional();
        this.cycleMethod = linearGradient.getCycleMethod();
        List<Stop> stopList = linearGradient.getStops();
        cstops = new CssStop[stopList.size()];
        for (int i = 0; i < cstops.length; i++) {
            Stop stop = stopList.get(i);
            cstops[i] = new CssStop(CssSize.of(stop.getOffset()), new CssColor(stop.getColor()));
        }
    }

    public LinearGradient getLinearGradient() {
        if (linearGradient == null) {
            int length = cstops.length;
            Stop[] stops = new Stop[length];
            DefaultUnitConverter cvrtr = DefaultUnitConverter.getInstance();
            for (int i = 0; i < length; i++) {
                CssStop cstop = cstops[i];
                double offset;
                if (cstop.offset() == null) {
                    int left = i, right = i;
                    for (; left > 0 && cstops[left].offset() == null; left--) {
                    }
                    for (; right < length - 1 && cstops[right].offset() == null; right++) {
                    }
                    CssSize leftOffsetSize = cstops[left].offset() == null ? CssSize.ZERO : cstops[left].offset();
                    CssSize rightOffsetSize = cstops[right].offset() == null ? CssSize.ONE : cstops[right].offset();
                    double leftOffset = cvrtr.convert(leftOffsetSize, UnitConverter.DEFAULT);
                    double rightOffset = cvrtr.convert(rightOffsetSize, UnitConverter.DEFAULT);
                    if (i == left) {
                        offset = leftOffset;
                    } else if (i == right) {
                        offset = rightOffset;
                    } else {
                        double mix = (double) (i - left) / (right - left);
                        offset = leftOffset * (1 - mix) + rightOffset * mix;
                    }
                } else {
                    offset = cvrtr.convert(cstop.offset(), UnitConverter.DEFAULT);
                }

                CssColor color = cstop.color();
                stops[i] = new Stop(offset, color == null ? Color.TRANSPARENT : color.getColor());
            }
            linearGradient = new LinearGradient(startX, startY, endX, endY, proportional, cycleMethod, stops);
        }
        return linearGradient;
    }

    @Override
    public Paint getPaint() {
        return getLinearGradient();
    }

    public Iterable<CssStop> getStops() {
        return Arrays.asList(cstops);
    }

    public double getStartX() {
        return startX;
    }

    public double getStartY() {
        return startY;
    }

    public double getEndX() {
        return endX;
    }

    public double getEndY() {
        return endY;
    }

    public boolean isProportional() {
        return proportional;
    }

    public CycleMethod getCycleMethod() {
        return cycleMethod;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CssLinearGradient other = (CssLinearGradient) obj;
        if (Double.doubleToLongBits(this.startX) != Double.doubleToLongBits(other.startX)) {
            return false;
        }
        if (Double.doubleToLongBits(this.startY) != Double.doubleToLongBits(other.startY)) {
            return false;
        }
        if (Double.doubleToLongBits(this.endX) != Double.doubleToLongBits(other.endX)) {
            return false;
        }
        if (Double.doubleToLongBits(this.endY) != Double.doubleToLongBits(other.endY)) {
            return false;
        }
        if (this.proportional != other.proportional) {
            return false;
        }
        if (!Objects.equals(this.linearGradient, other.linearGradient)) {
            return false;
        }
        if (this.cycleMethod != other.cycleMethod) {
            return false;
        }
        return Arrays.deepEquals(this.cstops, other.cstops);
    }

    @Override
    public String toString() {
        return "CssLinearGradient{" + "startX=" + startX + ", startY=" + startY + ", endX=" + endX + ", endY=" + endY + ", proportional=" + proportional + ", " + cycleMethod + ", stops=" + Arrays.toString(cstops) + '}';
    }
}
