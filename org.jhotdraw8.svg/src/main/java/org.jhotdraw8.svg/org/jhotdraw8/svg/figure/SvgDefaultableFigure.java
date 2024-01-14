/*
 * @(#)SvgDefaultableFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.svg.figure;

import javafx.css.StyleOrigin;
import javafx.scene.Node;
import javafx.scene.effect.BlendMode;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.css.converter.CssDoubleConverter;
import org.jhotdraw8.css.converter.CssKebabCaseEnumConverter;
import org.jhotdraw8.css.converter.CssListConverter;
import org.jhotdraw8.css.converter.CssMappedConverter;
import org.jhotdraw8.css.converter.CssPercentageConverter;
import org.jhotdraw8.css.converter.CssSizeConverter;
import org.jhotdraw8.css.value.CssDefaultableValue;
import org.jhotdraw8.css.value.CssDefaulting;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.converter.CssColorConverter;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.draw.css.value.NamedCssColor;
import org.jhotdraw8.draw.css.value.Paintable;
import org.jhotdraw8.draw.figure.DefaultableFigure;
import org.jhotdraw8.draw.key.DefaultableStyleableKey;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.fxcollection.typesafekey.TypeToken;
import org.jhotdraw8.icollection.SimpleImmutableList;
import org.jhotdraw8.icollection.immutable.ImmutableList;
import org.jhotdraw8.svg.css.SvgDefaultablePaint;
import org.jhotdraw8.svg.css.SvgPaintDefaulting;
import org.jhotdraw8.svg.io.SvgFontFamilyConverter;
import org.jhotdraw8.svg.key.SvgDefaultablePaintStyleableKey;
import org.jhotdraw8.svg.key.SvgDefaultablePaintStyleableMapAccessor;
import org.jhotdraw8.svg.text.SvgCssPaintableConverter;
import org.jhotdraw8.svg.text.SvgDisplay;
import org.jhotdraw8.svg.text.SvgFontSize;
import org.jhotdraw8.svg.text.SvgFontSizeConverter;
import org.jhotdraw8.svg.text.SvgShapeRendering;
import org.jhotdraw8.svg.text.SvgStrokeAlignmentConverter;
import org.jhotdraw8.svg.text.SvgTextAnchor;
import org.jhotdraw8.svg.text.SvgVisibility;

import java.util.Objects;

import static org.jhotdraw8.icollection.MapEntries.entry;
import static org.jhotdraw8.icollection.MapEntries.linkedHashMap;
import static org.jhotdraw8.icollection.MapEntries.of;
import static org.jhotdraw8.icollection.MapEntries.ofEntries;
import static org.jhotdraw8.svg.io.SvgFontFamilyConverter.GENERIC_FONT_FAMILY_SANS_SERIF;

/**
 * The following attributes can be defined on all SVG figures using the "defaulting"
 * mechanism.
 */
public interface SvgDefaultableFigure extends DefaultableFigure {
    /**
     * color.
     * <a href="https://www.w3.org/TR/SVGTiny12/painting.html#ColorProperty">link</a>
     */
    DefaultableStyleableKey<CssColor> COLOR_KEY = new DefaultableStyleableKey<>("color",
            new TypeToken<CssDefaultableValue<CssColor>>() {
            }, new CssColorConverter(true),
            new CssDefaultableValue<>(CssDefaulting.INHERIT, null), NamedCssColor.BLACK);
    /**
     * stop-color.
     */
    DefaultableStyleableKey<CssColor> STOP_COLOR_KEY = new DefaultableStyleableKey<>("stop-color",
            new TypeToken<CssDefaultableValue<CssColor>>() {
            }, new CssColorConverter(true),
            new CssDefaultableValue<>(CssDefaulting.INHERIT, null), NamedCssColor.BLACK);
    /**
     * stop-opacity.
     */
    DefaultableStyleableKey<CssSize> STOP_OPACITY_KEY = new DefaultableStyleableKey<>("stop-opacity",
            new TypeToken<CssDefaultableValue<CssSize>>() {
            }, new CssSizeConverter(true),
            new CssDefaultableValue<>(CssDefaulting.INHERIT, null), CssSize.ONE);
    /**
     * fill.
     * <a href="https://www.w3.org/TR/2018/CR-SVG2-20181004/painting.html#FillProperty">link</a>
     */
    SvgDefaultablePaintStyleableKey<Paintable> FILL_KEY = new SvgDefaultablePaintStyleableKey<>("fill",
            new TypeToken<SvgDefaultablePaint<Paintable>>() {
            }, new SvgCssPaintableConverter(true),
            new SvgDefaultablePaint<>(SvgPaintDefaulting.INHERIT, null), NamedCssColor.BLACK);
    /**
     * fill-rule.
     * <p>
     * <a href="https://www.w3.org/TR/SVG11/painting.html#FillRuleProperty">
     * SVG Tiny 1.2, The 'fill-rule' property</a>
     */
    @NonNull DefaultableStyleableKey<FillRule> FILL_RULE_KEY =
            new DefaultableStyleableKey<FillRule>("fill-rule",
                    new TypeToken<CssDefaultableValue<FillRule>>() {
                    },
                    new CssMappedConverter<>("fill-rule",
                            linkedHashMap(of("nonzero", FillRule.NON_ZERO,
                                    "evenodd", FillRule.EVEN_ODD
                            ))),
                    new CssDefaultableValue<>(CssDefaulting.INHERIT), FillRule.NON_ZERO
            );

    /**
     * font-family.
     * <p>
     * <a href="https://www.w3.org/TR/SVGTiny12/text.html#FontPropertiesUsedBySVG">link</a>
     */
    DefaultableStyleableKey<ImmutableList<String>> FONT_FAMILY_KEY = new DefaultableStyleableKey<>("font-family",
            new TypeToken<CssDefaultableValue<ImmutableList<String>>>() {
            }, new SvgFontFamilyConverter(),
            new CssDefaultableValue<>(CssDefaulting.INHERIT),
            SimpleImmutableList.of(GENERIC_FONT_FAMILY_SANS_SERIF)
    );

    /**
     * font-size.
     * <p>
     * <a href="https://www.w3.org/TR/SVGTiny12/text.html#FontPropertiesUsedBySVG">link</a>
     */
    DefaultableStyleableKey<SvgFontSize> FONT_SIZE_KEY = new DefaultableStyleableKey<>("font-size",
            new TypeToken<CssDefaultableValue<SvgFontSize>>() {
            }, new SvgFontSizeConverter(),
            new CssDefaultableValue<>(CssDefaulting.INHERIT),
            new SvgFontSize(SvgFontSize.SizeKeyword.MEDIUM, null)
    );
    /**
     * stroke.
     * <a href="https://www.w3.org/TR/2018/CR-SVG2-20181004/painting.html#StrokeProperty">link</a>
     */
    SvgDefaultablePaintStyleableKey<Paintable> STROKE_KEY = new SvgDefaultablePaintStyleableKey<>("stroke",
            new TypeToken<SvgDefaultablePaint<Paintable>>() {
            }, new SvgCssPaintableConverter(true),
            new SvgDefaultablePaint<>(SvgPaintDefaulting.INHERIT, null), null);

    /**
     * stroke-alignment.
     * <a href="https://www.w3.org/TR/2015/WD-svg-strokes-20150409/#SpecifyingStrokeAlignment">link</a>
     */
    DefaultableStyleableKey<StrokeType> STROKE_ALIGNMENT_KEY = new DefaultableStyleableKey<>(
            "stroke-alignment",
            new TypeToken<CssDefaultableValue<StrokeType>>() {
            },
            new SvgStrokeAlignmentConverter(false),
            new CssDefaultableValue<>(CssDefaulting.INHERIT, null), StrokeType.CENTERED);
    /**
     * stroke-dasharray.
     * <a href="https://www.w3.org/TR/SVGMobile12/painting.html#StrokeDasharrayProperty">link</a>
     */
    DefaultableStyleableKey<ImmutableList<Double>> STROKE_DASHARRAY_KEY = new DefaultableStyleableKey<>("stroke-dasharray",
            new TypeToken<CssDefaultableValue<ImmutableList<Double>>>() {
            }, new CssListConverter<Double>(new CssDoubleConverter(false), ", "),
            new CssDefaultableValue<ImmutableList<Double>>(CssDefaulting.INHERIT, null), null);
    /**
     * stroke-dashoffset.
     * <a href="https://www.w3.org/TR/SVGMobile12/painting.html#StrokeDashoffsetProperty">link</a>
     */
    DefaultableStyleableKey<Double> STROKE_DASHOFFSET_KEY =
            new DefaultableStyleableKey<Double>("stroke-dashoffset",
                    new TypeToken<CssDefaultableValue<Double>>() {
                    },
                    new CssPercentageConverter(false),
                    new CssDefaultableValue<>(CssDefaulting.INHERIT), 0.0);
    /**
     * fill-opacity.
     * <a href="https://www.w3.org/TR/2018/CR-SVG2-20181004/painting.html#FillOpacityProperty">link</a>
     */
    DefaultableStyleableKey<Double> FILL_OPACITY_KEY =
            new DefaultableStyleableKey<Double>("fill-opacity",
                    new TypeToken<CssDefaultableValue<Double>>() {
                    },
                    new CssPercentageConverter(false),
                    new CssDefaultableValue<>(CssDefaulting.INHERIT), 1.0);
    /**
     * stroke-opacity.
     * <a href="https://www.w3.org/TR/SVGMobile12/painting.html#StrokeOpacityProperty">link</a>
     */
    DefaultableStyleableKey<Double> STROKE_OPACITY_KEY =
            new DefaultableStyleableKey<Double>("stroke-opacity",
                    new TypeToken<CssDefaultableValue<Double>>() {
                    },
                    new CssPercentageConverter(false),
                    new CssDefaultableValue<>(CssDefaulting.INHERIT), 1.0);
    /**
     * text-anchor.
     * <p>
     * <a href="https://www.w3.org/TR/SVGTiny12/text.html#TextAlignmentProperties">
     * SVG Tiny 1.2, Text Alignment Properties</a>
     */
    @NonNull DefaultableStyleableKey<SvgTextAnchor> TEXT_ANCHOR_KEY =
            new DefaultableStyleableKey<SvgTextAnchor>("text-anchor",
                    new TypeToken<CssDefaultableValue<SvgTextAnchor>>() {
                    },
                    new CssKebabCaseEnumConverter<>(SvgTextAnchor.class),
                    new CssDefaultableValue<>(CssDefaulting.INHERIT), SvgTextAnchor.START
            );
    /**
     * shape-rendering.
     * <p>
     * <a href="https://www.w3.org/TR/SVGMobile12/painting.html#ShapeRenderingProperty">
     * SVG Tiny 1.2, The 'shape-rendering' property</a>
     */
    @NonNull DefaultableStyleableKey<SvgShapeRendering> SHAPE_RENDERING_KEY =
            new DefaultableStyleableKey<SvgShapeRendering>("shape-rendering",
                    new TypeToken<CssDefaultableValue<SvgShapeRendering>>() {
                    },
                    new CssMappedConverter<>("shape-rendering",
                            linkedHashMap(of("auto", SvgShapeRendering.AUTO,
                                    "optimizeSpeed", SvgShapeRendering.OPTIMIZE_SPEED,
                                    "crispEdges", SvgShapeRendering.CRISP_EDGES,
                                    "geometricPrecision", SvgShapeRendering.GEOMETRIC_PRECISION))),
                    new CssDefaultableValue<>(SvgShapeRendering.GEOMETRIC_PRECISION), SvgShapeRendering.AUTO
            );


    /**
     * stroke-miterlimit.
     * <a href="https://www.w3.org/TR/2015/WD-svg-strokes-20150409/#LineJoin">link</a>
     */
    DefaultableStyleableKey<Double> STROKE_MITERLIMIT_KEY = new DefaultableStyleableKey<Double>("stroke-miterlimit",
            new TypeToken<CssDefaultableValue<Double>>() {
            },
            new CssDoubleConverter(false),
            new CssDefaultableValue<>(CssDefaulting.INHERIT),
            4.0);
    /**
     * stroke-linecap.
     * <a href="https://www.w3.org/TR/2015/WD-svg-strokes-20150409/#LineJoin">link</a>
     */
    DefaultableStyleableKey<StrokeLineCap> STROKE_LINECAP_KEY = new DefaultableStyleableKey<StrokeLineCap>("stroke-linecap",
            new TypeToken<CssDefaultableValue<StrokeLineCap>>() {
            },
            new CssMappedConverter<>("stroke-linecap",
                    linkedHashMap(of("butt", StrokeLineCap.BUTT,
                            "round", StrokeLineCap.ROUND,
                            "square", StrokeLineCap.SQUARE))),
            new CssDefaultableValue<>(CssDefaulting.INHERIT),
            StrokeLineCap.BUTT);
    /**
     * stroke-linejoin.
     * <a href="https://www.w3.org/TR/2015/WD-svg-strokes-20150409/#LineJoin">link</a>
     */
    DefaultableStyleableKey<StrokeLineJoin> STROKE_LINEJOIN_KEY = new DefaultableStyleableKey<StrokeLineJoin>("stroke-linejoin",
            new TypeToken<CssDefaultableValue<StrokeLineJoin>>() {
            },
            new CssMappedConverter<>("stroke-linejoin",
                    linkedHashMap(of("miter", StrokeLineJoin.MITER,
                            "round", StrokeLineJoin.ROUND,
                            "bevel", StrokeLineJoin.BEVEL))),
            new CssDefaultableValue<>(CssDefaulting.INHERIT),
            StrokeLineJoin.MITER);
    /**
     * stroke-width.
     * <a href="https://www.w3.org/TR/2015/WD-svg-strokes-20150409/#StrokeWidth">link</a>
     */
    DefaultableStyleableKey<CssSize> STROKE_WIDTH_KEY = new DefaultableStyleableKey<CssSize>(
            "stroke-width",
            new TypeToken<CssDefaultableValue<CssSize>>() {
            },
            new CssSizeConverter(false),
            new CssDefaultableValue<>(CssDefaulting.INHERIT), CssSize.ONE);
    /**
     * visibility.
     * <a href="https://www.w3.org/TR/SVGTiny12/painting.html#DisplayProperty">link</a>
     */
    DefaultableStyleableKey<SvgVisibility> VISIBILITY_KEY = new DefaultableStyleableKey<>("visiblity",
            new TypeToken<CssDefaultableValue<SvgVisibility>>() {
            },
            new CssMappedConverter<SvgVisibility>("visiblity",
                    linkedHashMap(of("visible", SvgVisibility.VISIBLE,
                            "hidden", SvgVisibility.HIDDEN,
                            "collapse", SvgVisibility.COLLAPSE))),
            new CssDefaultableValue<>(CssDefaulting.INHERIT), SvgVisibility.VISIBLE);
    /**
     * mix-blend-mode.
     * <a href="https://developer.mozilla.org/de/docs/Web/CSS/mix-blend-mode">link</a>
     */
    DefaultableStyleableKey<BlendMode> MIX_BLEND_MODE_KEY = new DefaultableStyleableKey<>("mix-blend-mode",
            new TypeToken<CssDefaultableValue<BlendMode>>() {
            },
            new CssMappedConverter<BlendMode>("mix-blend-mode",
                    linkedHashMap(ofEntries(
                            entry("normal", BlendMode.SRC_OVER),
                            entry("mulitply", BlendMode.MULTIPLY),
                            entry("screen", BlendMode.SCREEN),
                            entry("overlay", BlendMode.OVERLAY),
                            entry("darken", BlendMode.DARKEN),
                            entry("lighten", BlendMode.LIGHTEN),
                            entry("color-dodge", BlendMode.COLOR_DODGE),
                            entry("color-burn", BlendMode.COLOR_BURN),
                            entry("hard-light", BlendMode.HARD_LIGHT),
                            entry("soft-light", BlendMode.SOFT_LIGHT),
                            entry("difference", BlendMode.DIFFERENCE),
                            entry("exclusion", BlendMode.EXCLUSION),
                            entry("hue", BlendMode.SRC_OVER),//FIXME
                            entry("saturation", BlendMode.SRC_OVER),//FIXME
                            entry("color", BlendMode.SRC_OVER),//FIXME
                            entry("luminosity", BlendMode.SRC_OVER)// FIXME
                    ))),
            new CssDefaultableValue<>(CssDefaulting.INHERIT), BlendMode.SRC_OVER);

    /**
     * display.
     * <p>
     * References:
     * <dl>
     *     <dt>SVG Tiny 1.2</dt><dd><a href="https://www.w3.org/TR/SVGTiny12/painting.html#DisplayProperty">w3.org</a></dd>
     *     <dt>SVG 2</dt><dd><a href="https://www.w3.org/TR/2018/CR-SVG2-20181004/render.html#VisibilityControl">w3.org</a></dd>
     * </dl>
     */
    @NonNull DefaultableStyleableKey<SvgDisplay> DISPLAY_KEY = new DefaultableStyleableKey<SvgDisplay>("display",
            new TypeToken<CssDefaultableValue<SvgDisplay>>() {
            },
            new CssMappedConverter<SvgDisplay>("display",
                    linkedHashMap(of("inline", SvgDisplay.INLINE)), true),
            new CssDefaultableValue<>(SvgDisplay.INLINE),// not inherited by default!
            SvgDisplay.INLINE);
    /**
     * opacity.
     * <a href="https://www.w3.org/TR/2011/REC-SVG11-20110816/masking.html#ObjectAndGroupOpacityProperties">link</a>
     */
    @NonNull DefaultableStyleableKey<Double> OPACITY_KEY =
            new DefaultableStyleableKey<Double>("opacity",
                    new TypeToken<CssDefaultableValue<Double>>() {
                    },
                    new CssPercentageConverter(false),
                    new CssDefaultableValue<>(CssDefaulting.INHERIT), 1.0);

    /**
     * Updates a figure node with all effect properties defined in this
     * interface.
     * <p>
     * Applies the following properties:
     * {@link #OPACITY_KEY}.
     * <p>
     * This method is intended to be used by {@link #updateNode}.
     *
     * @param ctx  the render context
     * @param node a node which was created with method {@link #createNode}.
     */
    default void applySvgDefaultableCompositingProperties(RenderContext ctx, @NonNull Node node) {
        node.setOpacity(getDefaultableStyledNonNull(OPACITY_KEY));
        BlendMode bmValue = getDefaultableStyledNonNull(MIX_BLEND_MODE_KEY);
        if (bmValue == BlendMode.SRC_OVER) {// Workaround: set SRC_OVER to null
            bmValue = null;
        }
        if (node.getBlendMode() != bmValue) {// Workaround: only set value if different
            node.setBlendMode(bmValue);
        }
    }

    /**
     * Applies fill properties to a {@link Shape} node.
     *
     * @param ctx   the render context
     * @param shape a shape node
     */
    default void applySvgDefaultableFillProperties(@NonNull RenderContext ctx, @NonNull Shape shape) {
        Paintable fill = getDefaultableStyled(FILL_KEY);
        if ((fill instanceof CssColor) && ("currentColor".equals(((CssColor) fill).getName()))) {
            fill = getDefaultableStyled(COLOR_KEY);
        }
        shape.setFill(Paintable.getPaint(fill));

        double fillOpacity = getDefaultableStyledNonNull(FILL_OPACITY_KEY);
        shape.setOpacity(fillOpacity);
    }

    /**
     * Applies stroke properties to a {@link Shape} node.
     *
     * @param ctx   the render context
     * @param shape a shape node
     */
    default void applySvgDefaultableStrokeProperties(@NonNull RenderContext ctx, @NonNull Shape shape) {
        Paintable stroke = getDefaultableStyled(STROKE_KEY);
        if ((stroke instanceof CssColor) && ("currentColor".equals(((CssColor) stroke).getName()))) {
            stroke = getDefaultableStyled(COLOR_KEY);
        }
        shape.setStroke(Paintable.getPaint(stroke));

        CssSize sw = getDefaultableStyledNonNull(STROKE_WIDTH_KEY);
        shape.setStrokeWidth(sw.getConvertedValue(ctx.getNonNull(RenderContext.UNIT_CONVERTER_KEY)));

        shape.setOpacity(getDefaultableStyledNonNull(STROKE_OPACITY_KEY));

        SvgShapeRendering shapeRendering = getDefaultableStyled(SHAPE_RENDERING_KEY);
        if (shapeRendering == SvgShapeRendering.CRISP_EDGES) {
            // stroke is translated by 0.5 pixels down right
            shape.setTranslateX(0.5);
            shape.setTranslateY(0.5);
        }
        shape.setStrokeLineCap(getDefaultableStyledNonNull(STROKE_LINECAP_KEY));
        shape.setStrokeLineJoin(getDefaultableStyledNonNull(STROKE_LINEJOIN_KEY));
        shape.setStrokeMiterLimit(getDefaultableStyledNonNull(STROKE_MITERLIMIT_KEY));
        shape.setStrokeDashOffset(getDefaultableStyledNonNull(STROKE_DASHOFFSET_KEY));
        ImmutableList<Double> dasharray = getDefaultableStyled(STROKE_DASHARRAY_KEY);
        if (dasharray == null) {
            shape.getStrokeDashArray().clear();
        } else {
            boolean allZeros = true;
            for (Double value : dasharray) {
                if (value > 0) {
                    allZeros = false;
                    break;
                }
            }
            if (allZeros) {
                shape.getStrokeDashArray().clear();
            } else {
                shape.getStrokeDashArray().setAll(dasharray.asCollection());
            }
        }

    }

    default void applySvgShapeProperties(RenderContext ctx, Shape fillShape, Shape strokeShape) {
        double strokeOpacity = getDefaultableStyledNonNull(STROKE_OPACITY_KEY);
        double fillOpacity = getDefaultableStyledNonNull(FILL_OPACITY_KEY);
        if (strokeOpacity == fillOpacity) {
            applySvgDefaultableFillProperties(ctx, fillShape);
            applySvgDefaultableStrokeProperties(ctx, fillShape);
            fillShape.setVisible(true);
            strokeShape.setVisible(false);
        } else {
            fillShape.setStroke(null);
            strokeShape.setFill(null);
            applySvgDefaultableFillProperties(ctx, fillShape);
            applySvgDefaultableStrokeProperties(ctx, strokeShape);
            fillShape.setVisible(true);
            strokeShape.setVisible(true);
        }
    }

    /**
     * Returns the styled value.
     *
     * @param <T> The value type
     * @param key The property key
     * @return The styled value.
     */
    default @Nullable <T extends Paintable> Paintable getDefaultableStyled(@NonNull SvgDefaultablePaintStyleableMapAccessor<T> key) {
        return getDefaultableStyled(StyleOrigin.INLINE, key);
    }

    default @Nullable <T extends Paintable> Paintable getDefaultableStyled(@NonNull StyleOrigin origin, @NonNull SvgDefaultablePaintStyleableMapAccessor<T> key) {
        // FIXME REVERT does not work this way, must use getStyled(origin,key) for _starting a search at the specified origin_ value
        SvgDefaultablePaint<T> dv = Objects.requireNonNull(getStyled(origin == StyleOrigin.INLINE ? null : origin, key));
        if (dv.getDefaulting() == null) {
            return dv.getValue();
        }
        switch (dv.getDefaulting()) {
            case INHERIT:
                if (getParent() instanceof SvgDefaultableFigure) {
                    return ((SvgDefaultableFigure) getParent()).getDefaultableStyled(key);
                } else {
                    return key.getInitialValue();
                }
            case CURRENT_COLOR:
                return getDefaultableStyled(COLOR_KEY);
            default:
                throw new UnsupportedOperationException("unsupported defaulting: " + dv.getDefaulting());
        }
    }

}
