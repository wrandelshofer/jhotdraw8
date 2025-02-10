/*
 * @(#)DefaultFigureFactory.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.io;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Paint;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import org.jhotdraw8.base.converter.DefaultConverter;
import org.jhotdraw8.base.converter.IdFactory;
import org.jhotdraw8.base.text.RegexReplace;
import org.jhotdraw8.css.converter.DoubleCssConverter;
import org.jhotdraw8.css.converter.KebabCaseEnumCssConverter;
import org.jhotdraw8.css.converter.ListCssConverter;
import org.jhotdraw8.css.converter.SizeCssConverter;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.connector.Connector;
import org.jhotdraw8.draw.css.converter.ColorCssConverter;
import org.jhotdraw8.draw.css.converter.Dimension2DCssConverter;
import org.jhotdraw8.draw.css.converter.EffectCssConverter;
import org.jhotdraw8.draw.css.converter.FontCssConverter;
import org.jhotdraw8.draw.css.converter.InsetsConverter;
import org.jhotdraw8.draw.css.converter.InsetsCssConverter;
import org.jhotdraw8.draw.css.converter.PaintCssConverter;
import org.jhotdraw8.draw.css.converter.PaintableCssConverter;
import org.jhotdraw8.draw.css.converter.PaperSizeCssConverter;
import org.jhotdraw8.draw.css.converter.Point2DConverter;
import org.jhotdraw8.draw.css.converter.Point2DCssConverter;
import org.jhotdraw8.draw.css.converter.Point3DCssConverter;
import org.jhotdraw8.draw.css.converter.Rectangle2DCssConverter;
import org.jhotdraw8.draw.css.converter.RegexCssConverter;
import org.jhotdraw8.draw.css.converter.StrokeStyleCssConverter;
import org.jhotdraw8.draw.css.converter.TransformCssConverter;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.draw.css.value.CssDimension2D;
import org.jhotdraw8.draw.css.value.CssFont;
import org.jhotdraw8.draw.css.value.CssInsets;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.css.value.CssPoint3D;
import org.jhotdraw8.draw.css.value.CssRectangle2D;
import org.jhotdraw8.draw.css.value.CssStrokeStyle;
import org.jhotdraw8.draw.css.value.Paintable;
import org.jhotdraw8.draw.figure.AbstractDrawing;
import org.jhotdraw8.draw.figure.BezierPathFigure;
import org.jhotdraw8.draw.figure.ClippingFigure;
import org.jhotdraw8.draw.figure.CombinedPathFigure;
import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.draw.figure.ElbowConnectionWithMarkersFigure;
import org.jhotdraw8.draw.figure.EllipseFigure;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.GroupFigure;
import org.jhotdraw8.draw.figure.IconPosition;
import org.jhotdraw8.draw.figure.ImageFigure;
import org.jhotdraw8.draw.figure.LabelAutorotate;
import org.jhotdraw8.draw.figure.LabelFigure;
import org.jhotdraw8.draw.figure.LayerFigure;
import org.jhotdraw8.draw.figure.LineConnectionFigure;
import org.jhotdraw8.draw.figure.LineConnectionWithMarkersFigure;
import org.jhotdraw8.draw.figure.LineFigure;
import org.jhotdraw8.draw.figure.MarkerStrokableFigure;
import org.jhotdraw8.draw.figure.PageFigure;
import org.jhotdraw8.draw.figure.PageLabelFigure;
import org.jhotdraw8.draw.figure.PolygonFigure;
import org.jhotdraw8.draw.figure.PolylineFigure;
import org.jhotdraw8.draw.figure.RectangleFigure;
import org.jhotdraw8.draw.figure.RegionFigure;
import org.jhotdraw8.draw.figure.SimpleLayeredDrawing;
import org.jhotdraw8.draw.figure.SliceFigure;
import org.jhotdraw8.draw.figure.StrokableFigure;
import org.jhotdraw8.draw.figure.StyleableFigure;
import org.jhotdraw8.draw.figure.TextAreaFigure;
import org.jhotdraw8.draw.figure.TextFigure;
import org.jhotdraw8.draw.figure.TextStrokeableFigure;
import org.jhotdraw8.draw.figure.TransformableFigure;
import org.jhotdraw8.draw.xml.converter.BezierPathXmlConverter;
import org.jhotdraw8.draw.xml.converter.ConnectorXmlConverter;
import org.jhotdraw8.draw.xml.converter.FXPathElementsXmlConverter;
import org.jhotdraw8.draw.xml.converter.PathConnectionBezierPathXmlConverter;
import org.jhotdraw8.draw.xml.converter.Point2DXmlConverter;
import org.jhotdraw8.draw.xml.converter.Point3DXmlConverter;
import org.jhotdraw8.draw.xml.converter.Rectangle2DXmlConverter;
import org.jhotdraw8.draw.xml.converter.SvgPathXmlConverter;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.SimpleParameterizedType;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jhotdraw8.xml.converter.BooleanXmlConverter;
import org.jhotdraw8.xml.converter.ObjectReferenceXmlConverter;
import org.jhotdraw8.xml.converter.UriXmlConverter;
import org.jhotdraw8.xml.converter.UrlXmlConverter;
import org.jhotdraw8.xml.converter.WordSetXmlConverter;

import java.net.URI;
import java.net.URL;
import java.util.Set;
import java.util.logging.Logger;

/**
 * DefaultFigureFactory.
 *
 */
public class DefaultFigureFactory extends AbstractFigureFactory {

    public DefaultFigureFactory() {
        this(new SimpleFigureIdFactory());
    }

    public DefaultFigureFactory(IdFactory idFactory) {
        super(idFactory);

        addFigureKeysAndNames("Layer", LayerFigure.class);
        addFigureKeysAndNames("Clipping", ClippingFigure.class);
        addFigureKeysAndNames("Rectangle", RectangleFigure.class);
        addFigureKeysAndNames("Region", RegionFigure.class);
        addFigureKeysAndNames("Slice", SliceFigure.class);
        addFigureKeysAndNames("Group", GroupFigure.class);
        addFigureKeysAndNames("Polyline", PolylineFigure.class);
        addFigureKeysAndNames("Polygon", PolygonFigure.class);
        addFigureKeysAndNames("Page", PageFigure.class);
        addFigureKeysAndNames("CombinedPath", CombinedPathFigure.class);

        {
            Set<MapAccessor<?>> keys = Figure.getDeclaredAndInheritedMapAccessors(AbstractDrawing.class).toMutable();
            keys.remove(Drawing.USER_AGENT_STYLESHEETS);
            keys.remove(Drawing.AUTHOR_STYLESHEETS);
            keys.remove(Drawing.INLINE_STYLESHEETS);
            keys.remove(Drawing.DOCUMENT_HOME);
            addFigureKeysAndNames("Drawing", SimpleLayeredDrawing.class, keys);
        }

        addFigureKeysAndNames("Text", TextFigure.class);
        addFigureKeysAndNames("TextArea", TextAreaFigure.class);
        addFigureKeysAndNames("Label", LabelFigure.class);
        addFigureKeysAndNames("PageLabel", PageLabelFigure.class);

        addFigureKeysAndNames("Line", LineFigure.class);
        addFigureKeysAndNames("Ellipse", EllipseFigure.class);
        addFigureKeysAndNames("LineConnection", LineConnectionFigure.class);
        addFigureKeysAndNames("LineConnectionWithMarkers", LineConnectionWithMarkersFigure.class);
        addFigureKeysAndNames("ElbowLineConnectionWithMarkers", ElbowConnectionWithMarkersFigure.class);
        addFigureKeysAndNames("Image", ImageFigure.class);
        addFigureKeysAndNames("BezierPath", BezierPathFigure.class);

        addConverterForType(BlendMode.class, new KebabCaseEnumCssConverter<>(BlendMode.class));
        addConverterForType(Boolean.class, new BooleanXmlConverter());
        addConverterForType(CombinedPathFigure.CagOperation.class, new KebabCaseEnumCssConverter<>(CombinedPathFigure.CagOperation.class));
        addConverterForType(Connector.class, new ConnectorXmlConverter());
        addConverterForType(CssColor.class, new ColorCssConverter());
        addConverterForType(CssDimension2D.class, new Dimension2DCssConverter(false));
        addConverterForType(CssFont.class, new FontCssConverter(false));
        addConverterForType(CssInsets.class, new InsetsCssConverter(false));
        addConverterForType(CssPoint2D.class, new Point2DCssConverter(false));
        addConverterForType(CssPoint3D.class, new Point3DCssConverter(false));
        addConverterForType(CssRectangle2D.class, new Rectangle2DCssConverter(false));
        addConverterForType(CssSize.class, new SizeCssConverter(false));
        addConverterForType(CssStrokeStyle.class, new StrokeStyleCssConverter(false));
        addConverterForType(Double.class, new DoubleCssConverter(false));
        addConverterForType(Effect.class, new EffectCssConverter());
        addConverterForType(Figure.class, new ObjectReferenceXmlConverter<>(Figure.class));
        addConverterForType(FillRule.class, new KebabCaseEnumCssConverter<>(FillRule.class));
        addConverterForType(FontPosture.class, new KebabCaseEnumCssConverter<>(FontPosture.class));
        addConverterForType(FontWeight.class, new KebabCaseEnumCssConverter<>(FontWeight.class));
        addConverterForType(HPos.class, new KebabCaseEnumCssConverter<>(HPos.class));
        addConverterForType(IconPosition.class, new KebabCaseEnumCssConverter<>(IconPosition.class));
        addConverterForType(Insets.class, new InsetsConverter(false));
        addConverterForType(LabelAutorotate.class, new KebabCaseEnumCssConverter<>(LabelAutorotate.class));
        addConverterForType(Paint.class, new PaintCssConverter(true));
        addConverterForType(Paintable.class, new PaintableCssConverter(true));
        addConverterForType(Point2D.class, new Point2DXmlConverter());
        addConverterForType(Point3D.class, new Point3DXmlConverter());
        addConverterForType(Rectangle2D.class, new Rectangle2DXmlConverter());
        addConverterForType(RegexReplace.class, new RegexCssConverter(true));// FIXME remove from JHotDraw
        addConverterForType(SVGPath.class, new SvgPathXmlConverter());
        addConverterForType(String.class, new DefaultConverter());
        addConverterForType(StrokeLineCap.class, new KebabCaseEnumCssConverter<>(StrokeLineCap.class));
        addConverterForType(StrokeLineJoin.class, new KebabCaseEnumCssConverter<>(StrokeLineJoin.class));
        addConverterForType(StrokeType.class, new KebabCaseEnumCssConverter<>(StrokeType.class));
        addConverterForType(TextAlignment.class, new KebabCaseEnumCssConverter<>(TextAlignment.class));
        addConverterForType(URI.class, new UriXmlConverter());
        addConverterForType(URL.class, new UrlXmlConverter());
        addConverterForType(VPos.class, new KebabCaseEnumCssConverter<>(VPos.class));
        addConverterForType(new SimpleParameterizedType(PersistentList.class, PathElement.class), new FXPathElementsXmlConverter());

        addConverter(org.jhotdraw8.draw.figure.AbstractPathConnectionWithMarkersFigure.PATH, new PathConnectionBezierPathXmlConverter());
        addConverter(PageFigure.PAPER_SIZE, new PaperSizeCssConverter());
        addConverter(StyleableFigure.STYLE_CLASS, new WordSetXmlConverter());
        addConverter(TextStrokeableFigure.TEXT_STROKE_DASH_ARRAY, new ListCssConverter<>(new SizeCssConverter(false)));
        addConverter(StrokableFigure.STROKE_DASH_ARRAY, new ListCssConverter<>(new SizeCssConverter(false)));
        addConverter(TransformableFigure.TRANSFORMS, new ListCssConverter<>(new TransformCssConverter(false)));
        addConverter(PolylineFigure.POINTS, new ListCssConverter<>(new Point2DConverter(false)));
        addConverter(BezierPathFigure.PATH, new BezierPathXmlConverter(true));
        addConverter(StrokableFigure.STROKE_DASH_ARRAY, new ListCssConverter<>(new SizeCssConverter(false)));
        addConverter(MarkerStrokableFigure.MARKER_STROKE_DASH_ARRAY, new ListCssConverter<>(new SizeCssConverter(false)));

        removeKey(StyleableFigure.PSEUDO_CLASS);

        checkConverters(false, Logger.getLogger(DefaultFigureFactory.class.getName())::warning);
    }

}
