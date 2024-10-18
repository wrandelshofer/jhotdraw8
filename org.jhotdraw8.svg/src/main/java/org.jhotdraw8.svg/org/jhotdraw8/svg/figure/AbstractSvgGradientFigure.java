/*
 * @(#)AbstractSvgGradientFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.svg.figure;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import org.jhotdraw8.base.converter.MappedConverter;
import org.jhotdraw8.css.value.CssDefaultableValue;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.draw.css.value.NamedCssColor;
import org.jhotdraw8.draw.css.value.Paintable;
import org.jhotdraw8.draw.figure.AbstractCompositeFigure;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.Grouping;
import org.jhotdraw8.draw.figure.HideableFigure;
import org.jhotdraw8.draw.figure.LockableFigure;
import org.jhotdraw8.draw.figure.NonTransformableFigure;
import org.jhotdraw8.draw.figure.ResizableFigure;
import org.jhotdraw8.draw.figure.StyleableFigure;
import org.jhotdraw8.draw.key.NonNullObjectStyleableKey;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.fxcollection.typesafekey.NonNullKey;
import org.jhotdraw8.fxcollection.typesafekey.NonNullObjectKey;
import org.jhotdraw8.fxcollection.typesafekey.SimpleParameterizedType;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jhotdraw8.svg.css.SvgDefaultablePaint;
import org.jhotdraw8.svg.text.SvgGradientUnits;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;

/**
 * Represents an SVG 'linearGradient' element.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractSvgGradientFigure extends AbstractCompositeFigure
        implements Grouping, ResizableFigure, NonTransformableFigure, HideableFigure, StyleableFigure, LockableFigure,
        SvgDefaultableFigure,
        SvgElementFigure, Paintable {

    /**
     * <a href="https://www.w3.org/TR/SVG11/pservers.html#LinearGradientElementGradientUnitsAttribute">w3.org</a>
     */
    public static final NonNullObjectStyleableKey<SvgGradientUnits> GRADIENT_UNITS =
            new NonNullObjectStyleableKey<>("gradientUnits", SvgGradientUnits.class,
                    new MappedConverter<>(Map.of(
                            "userSpaceOnUse", SvgGradientUnits.USER_SPACE_ON_USE,
                            "objectBoundingBox", SvgGradientUnits.OBJECT_BOUNDING_BOX
                    )),
                    SvgGradientUnits.OBJECT_BOUNDING_BOX
            );

    /**
     * <a href="https://www.w3.org/TR/SVG11/pservers.html#LinearGradientElementSpreadMethodAttribute">w3.org</a>
     */
    public static final NonNullObjectStyleableKey<CycleMethod> SPREAD_METHOD =
            new NonNullObjectStyleableKey<>("spreadMethod", CycleMethod.class,
                    new MappedConverter<>(Map.of(
                            "pad", CycleMethod.NO_CYCLE,
                            "reflect", CycleMethod.REFLECT,
                            "repeat", CycleMethod.REPEAT
                    )),
                    CycleMethod.NO_CYCLE
            );


    public static final NonNullKey<PersistentList<SvgStop>> STOPS = new NonNullObjectKey<>("stops",
            new SimpleParameterizedType(PersistentList.class, SvgStop.class), VectorList.of());

    public AbstractSvgGradientFigure() {
        set(VISIBLE, false);
    }

    @Override
    public Node createNode(RenderContext drawingView) {
        Group g = new Group();
        g.setAutoSizeChildren(false);
        g.setManaged(false);
        g.setVisible(false);
        return g;
    }

    @Override
    public @Nullable Paint getPaint() {
        return getPaint(null);
    }


    protected ArrayList<Stop> getStops(PersistentList<SvgStop> cssStops) {
        ArrayList<Stop> stops = new ArrayList<>(cssStops.size());
        double maxOffset = 0;
        for (SvgStop cssStop : cssStops) {
            SvgDefaultablePaint<CssColor> colorDef = cssStop.color();
            CssColor cssColor;
            if (colorDef == null) {
                cssColor = NamedCssColor.BLACK;
            } else if (colorDef.getDefaulting() == null) {
                cssColor = colorDef.getValue();
            } else {
                cssColor = switch (colorDef.getDefaulting()) {
                    default -> getDefaultableStyled(STOP_COLOR_KEY);
                    case CURRENT_COLOR -> getDefaultableStyled(COLOR_KEY);
                };
            }
            Double offset = cssStop.offset();
            CssDefaultableValue<CssSize> opacityDef = cssStop.opacity();
            CssSize opacity;
            if (opacityDef.getDefaulting() != null) {
                opacity = switch (opacityDef.getDefaulting()) {
                    case INHERIT -> getDefaultableStyled(STOP_OPACITY_KEY);
                    default -> CssSize.ONE;
                };
            } else {
                opacity = opacityDef.getValue();
            }
            Color color;
            color = cssColor == null ? Color.BLACK : cssColor.getColor();
            if (opacity != null && opacity.getConvertedValue() != 1.0) {
                color = Color.color(color.getRed(), color.getGreen(), color.getBlue(), opacity.getConvertedValue());
            }
            if (offset != null) {
                maxOffset = Math.max(offset, maxOffset);
                stops.add(new Stop(maxOffset, color));
            }
        }
        return stops;
    }


    @Override
    public void updateNode(RenderContext ctx, Node n) {
    }

    @Override
    public boolean isSuitableParent(Figure newParent) {
        return true;
    }


    @Override
    public boolean isSuitableChild(Figure newChild) {
        return true;
    }

    @Override
    public void reshapeInLocal(CssSize x, CssSize y, CssSize width, CssSize height) {
        // does nothing
    }
}
