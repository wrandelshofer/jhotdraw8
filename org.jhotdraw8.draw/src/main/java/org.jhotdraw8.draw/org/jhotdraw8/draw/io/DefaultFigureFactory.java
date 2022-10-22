/*
 * @(#)DefaultFigureFactory.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
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
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.collection.reflect.TypeToken;
import org.jhotdraw8.collection.typesafekey.MapAccessor;
import org.jhotdraw8.css.CssSize;
import org.jhotdraw8.css.CssStrokeStyle;
import org.jhotdraw8.draw.connector.Connector;
import org.jhotdraw8.draw.css.CssColor;
import org.jhotdraw8.draw.css.CssDimension2D;
import org.jhotdraw8.draw.css.CssFont;
import org.jhotdraw8.draw.css.CssInsets;
import org.jhotdraw8.draw.css.CssPoint2D;
import org.jhotdraw8.draw.css.CssPoint3D;
import org.jhotdraw8.draw.css.CssRectangle2D;
import org.jhotdraw8.draw.css.Paintable;
import org.jhotdraw8.draw.css.converter.CssColorConverter;
import org.jhotdraw8.draw.css.converter.CssDimension2DConverter;
import org.jhotdraw8.draw.css.converter.CssDoubleConverter;
import org.jhotdraw8.draw.css.converter.CssEffectConverter;
import org.jhotdraw8.draw.css.converter.CssFontConverter;
import org.jhotdraw8.draw.css.converter.CssInsetsConverter;
import org.jhotdraw8.draw.css.converter.CssKebabCaseEnumConverter;
import org.jhotdraw8.draw.css.converter.CssListConverter;
import org.jhotdraw8.draw.css.converter.CssPaintConverter;
import org.jhotdraw8.draw.css.converter.CssPaintableConverter;
import org.jhotdraw8.draw.css.converter.CssPaperSizeConverter;
import org.jhotdraw8.draw.css.converter.CssPoint2DConverter;
import org.jhotdraw8.draw.css.converter.CssPoint3DConverter;
import org.jhotdraw8.draw.css.converter.CssRectangle2DConverter;
import org.jhotdraw8.draw.css.converter.CssRegexConverter;
import org.jhotdraw8.draw.css.converter.CssSizeConverter;
import org.jhotdraw8.draw.css.converter.CssStrokeStyleConverter;
import org.jhotdraw8.draw.css.converter.CssTransformConverter;
import org.jhotdraw8.draw.css.converter.InsetsConverter;
import org.jhotdraw8.draw.css.converter.Point2DConverter;
import org.jhotdraw8.draw.figure.AbstractDrawing;
import org.jhotdraw8.draw.figure.BezierFigure;
import org.jhotdraw8.draw.figure.ClippingFigure;
import org.jhotdraw8.draw.figure.CombinedPathFigure;
import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.draw.figure.EllipseFigure;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.GroupFigure;
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
import org.jhotdraw8.xml.text.XmlBezierNodeListConverter;
import org.jhotdraw8.xml.text.XmlBooleanConverter;
import org.jhotdraw8.xml.text.XmlConnectorConverter;
import org.jhotdraw8.xml.text.XmlFXSvgPathConverter;
import org.jhotdraw8.xml.text.XmlObjectReferenceConverter;
import org.jhotdraw8.xml.text.XmlPoint2DConverter;
import org.jhotdraw8.xml.text.XmlPoint3DConverter;
import org.jhotdraw8.xml.text.XmlRectangle2DConverter;
import org.jhotdraw8.xml.text.XmlSvgPathConverter;
import org.jhotdraw8.xml.text.XmlUriConverter;
import org.jhotdraw8.xml.text.XmlUrlConverter;
import org.jhotdraw8.xml.text.XmlWordSetConverter;

import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * DefaultFigureFactory.
 *
 * @author Werner Randelshofer
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
            Set<MapAccessor<?>> keys = new HashSet<>(Figure.getDeclaredAndInheritedMapAccessors(AbstractDrawing.class));
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
        addFigureKeysAndNames("Image", ImageFigure.class);
        addFigureKeysAndNames("BezierPath", BezierFigure.class);

        addConverterForType(String.class, new DefaultConverter());
        addConverterForType(Point2D.class, new XmlPoint2DConverter());
        addConverterForType(Point3D.class, new XmlPoint3DConverter());
        addConverterForType(SVGPath.class, new XmlSvgPathConverter());
        addConverterForType(Insets.class, new InsetsConverter(false));
        addConverterForType(Double.class, new CssDoubleConverter(false));
        addConverterForType(URL.class, new XmlUrlConverter());
        addConverterForType(URI.class, new XmlUriConverter());
        addConverterForType(Connector.class, new XmlConnectorConverter());
        addConverterForType(Paint.class, new CssPaintConverter(true));
        addConverterForType(Paintable.class, new CssPaintableConverter(true));
        addConverterForType(CssColor.class, new CssColorConverter());
        addConverterForType(Boolean.class, new XmlBooleanConverter());
        addConverterForType(TextAlignment.class, new CssKebabCaseEnumConverter<>(TextAlignment.class));
        addConverterForType(CombinedPathFigure.CagOperation.class, new CssKebabCaseEnumConverter<>(CombinedPathFigure.CagOperation.class));
        addConverterForType(VPos.class, new CssKebabCaseEnumConverter<>(VPos.class));
        addConverterForType(HPos.class, new CssKebabCaseEnumConverter<>(HPos.class));
        addConverterForType(CssFont.class, new CssFontConverter(false));
        addConverterForType(Rectangle2D.class, new XmlRectangle2DConverter());
        addConverterForType(BlendMode.class, new CssKebabCaseEnumConverter<>(BlendMode.class));
        addConverterForType(Effect.class, new CssEffectConverter());
        addConverterForType(Figure.class, new XmlObjectReferenceConverter<>(Figure.class));
        addConverterForType(CssSize.class, new CssSizeConverter(false));
        addConverterForType(CssInsets.class, new CssInsetsConverter(false));
        addConverterForType(CssRectangle2D.class, new CssRectangle2DConverter(false));
        addConverterForType(CssDimension2D.class, new CssDimension2DConverter(false));
        addConverterForType(CssPoint2D.class, new CssPoint2DConverter(false));
        addConverterForType(CssPoint3D.class, new CssPoint3DConverter(false));
        addConverterForType(FillRule.class, new CssKebabCaseEnumConverter<>(FillRule.class));
        addConverterForType(FontWeight.class, new CssKebabCaseEnumConverter<>(FontWeight.class));
        addConverterForType(FontPosture.class, new CssKebabCaseEnumConverter<>(FontPosture.class));
        addConverterForType(LabelAutorotate.class, new CssKebabCaseEnumConverter<>(LabelAutorotate.class));
        addConverterForType(RegexReplace.class, new CssRegexConverter(true));// FIXME remove from JHotDraw
        addConverterForType(StrokeLineJoin.class, new CssKebabCaseEnumConverter<>(StrokeLineJoin.class));
        addConverterForType(StrokeLineCap.class, new CssKebabCaseEnumConverter<>(StrokeLineCap.class));
        addConverterForType(StrokeType.class, new CssKebabCaseEnumConverter<>(StrokeType.class));
        addConverterForType(CssStrokeStyle.class, new CssStrokeStyleConverter(false));
        addConverterForType(new TypeToken<ImmutableList<PathElement>>() {
        }.getType(), new XmlFXSvgPathConverter());

        addConverter(PageFigure.PAPER_SIZE, new CssPaperSizeConverter());
        addConverter(StyleableFigure.STYLE_CLASS, new XmlWordSetConverter());
        addConverter(TextStrokeableFigure.TEXT_STROKE_DASH_ARRAY, new CssListConverter<>(new CssSizeConverter(false)));
        addConverter(StrokableFigure.STROKE_DASH_ARRAY, new CssListConverter<>(new CssSizeConverter(false)));
        addConverter(TransformableFigure.TRANSFORMS, new CssListConverter<>(new CssTransformConverter(false)));
        addConverter(PolylineFigure.POINTS, new CssListConverter<>(new Point2DConverter(false)));
        addConverter(BezierFigure.PATH, new XmlBezierNodeListConverter(true));
        addConverter(StrokableFigure.STROKE_DASH_ARRAY, new CssListConverter<>(new CssSizeConverter(false)));
        addConverter(MarkerStrokableFigure.MARKER_STROKE_DASH_ARRAY, new CssListConverter<>(new CssSizeConverter(false)));

        removeKey(StyleableFigure.PSEUDO_CLASS);

        checkConverters(false, Logger.getLogger(DefaultFigureFactory.class.getName())::warning);
    }

}
