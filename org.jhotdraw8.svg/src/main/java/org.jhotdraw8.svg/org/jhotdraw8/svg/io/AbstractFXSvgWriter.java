/*
 * @(#)AbstractFXSvgWriter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.svg.io;

import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdFactory;
import org.jhotdraw8.base.converter.SimpleIdFactory;
import org.jhotdraw8.css.converter.DoubleCssConverter;
import org.jhotdraw8.css.converter.ListCssConverter;
import org.jhotdraw8.css.value.CssDimension2D;
import org.jhotdraw8.css.value.CssRectangle2D;
import org.jhotdraw8.fxbase.beans.AbstractPropertyBean;
import org.jhotdraw8.geom.*;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jhotdraw8.svg.text.SvgPaintCssConverter;
import org.jhotdraw8.svg.text.SvgTransformConverter;
import org.jhotdraw8.xml.IndentingXMLStreamWriter;
import org.jhotdraw8.xml.converter.DoubleXmlConverter;
import org.jhotdraw8.xml.converter.FloatXmlConverter;
import org.jhotdraw8.xml.converter.IntegerXmlConverter;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.dom.DOMResult;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.*;
import java.util.List;

import static org.jhotdraw8.draw.io.BitmapExportOutputFormat.fromFXImage;

public abstract class AbstractFXSvgWriter extends AbstractPropertyBean implements SvgSceneGraphWriter {
    public static final String SVG_MIME_TYPE = "image/svg+xml";
    public static final String SVG_NS = "http://www.w3.org/2000/svg";
    protected static final String XLINK_NS = "http://www.w3.org/1999/xlink";
    protected static final String XLINK_PREFIX = "xlink";
    protected final DoubleXmlConverter nb = new DoubleXmlConverter();
    protected final IntegerXmlConverter nbi = new IntegerXmlConverter();
    protected final FloatXmlConverter nbf = new FloatXmlConverter();
    private final @Nullable Object imageUriKey;
    private final Converter<PersistentList<Double>> doubleList = new ListCssConverter<>(new DoubleCssConverter(false));
    private final Converter<Paint> paintConverter = new SvgPaintCssConverter(true);
    private final @Nullable Object skipKey;
    private final Converter<PersistentList<Transform>> tx = new ListCssConverter<>(new SvgTransformConverter(false));
    protected final IdFactory idFactory = new SimpleIdFactory();

    /**
     * @param imageUriKey this property is used to retrieve an URL from an
     *                    ImageView. If an ImageView does not have an URL,
     *                    then the exporter includes the image with a data URL.
     * @param skipKey     this property is used to retrieve a Boolean from a Node.
     *                    If the Boolean is true, then the node is skipped.
     */
    public AbstractFXSvgWriter(@Nullable Object imageUriKey, @Nullable Object skipKey) {
        this.imageUriKey = imageUriKey;
        this.skipKey = skipKey;
    }

    /**
     * Draws or measures a paragraph of text at the specified y location and
     * the bounds of the paragraph.
     *
     * @param styledText     the text of the paragraph.
     * @param verticalPos    the top bound of the paragraph
     * @param maxVerticalPos the bottom bound of the paragraph
     * @param leftMargin     the left bound of the paragraph
     * @param rightMargin    the right bound of the paragraph
     * @param tabStops       an array with tab stops
     * @param tabCount       the number of entries in tabStops which contain actual
     *                       values
     * @return Returns the actual bounds of the paragraph.
     */
    private Rectangle2D.Double drawParagraph(XMLStreamWriter w,
                                             FontRenderContext frc, String
                                                     paragraph, AttributedCharacterIterator styledText,
                                             float verticalPos, float maxVerticalPos, float leftMargin,
                                             float rightMargin, float[] tabStops, int tabCount,
                                             TextAlignment textAlignment, float lineSpacing) throws XMLStreamException {
        // This method is based on the code sample given
        // in the class comment of java.awt.font.LineBreakMeasurer,
        // assume styledText is an AttributedCharacterIterator, and the number
        // of tabs in styledText is tabCount


        Rectangle2D.Double paragraphBounds = new Rectangle2D.Double(leftMargin, verticalPos, 0, 0);
        int[] tabLocations = new int[tabCount + 1];

        int i = 0;
        for (char c = styledText.first(); c != AttributedCharacterIterator.DONE; c = styledText.next()) {
            if (c == '\t') {
                tabLocations[i++] = styledText.getIndex();
            }
        }
        tabLocations[tabCount] = styledText.getEndIndex() - 1;

        // Now tabLocations has an entry for every tab's offset in
        // the text.  For convenience, the last entry is tabLocations
        // is the offset of the last character in the text.
        LineBreakMeasurer measurer = new LineBreakMeasurer(styledText, frc);
        int currentTab = 0;
        int textIndex = 0;
        while (measurer.getPosition() < styledText.getEndIndex()
                && verticalPos <= maxVerticalPos) {

            // Lay out and draw each line.  All segments on a line
            // must be computed before any drawing can occur, since
            // we must know the largest ascent on the line.
            // TextLayouts are computed and stored in a List;
            // their horizontal positions are stored in a parallel
            // List.

            // lineContainsText is true after first segment is drawn
            boolean lineContainsText = false;
            boolean lineComplete = false;
            float maxAscent = 0, maxDescent = 0;
            float horizontalPos = leftMargin;
            List<TextLayout> layouts = new ArrayList<>();
            List<Float> penPositions = new ArrayList<>();

            int first = 0;

            while (!lineComplete && verticalPos <= maxVerticalPos) {
                float wrappingWidth = rightMargin - horizontalPos;
                TextLayout layout;
                layout = measurer.nextLayout(wrappingWidth,
                        tabLocations[currentTab] + 1,
                        lineContainsText);

                // layout can be null if lineContainsText is true
                if (layout != null) {
                    layouts.add(layout);
                    penPositions.add(horizontalPos);
                    horizontalPos += layout.getAdvance();
                    maxAscent = Math.max(maxAscent, layout.getAscent());
                    maxDescent = Math.max(maxDescent,
                            layout.getDescent() + layout.getLeading());
                } else {
                    lineComplete = true;
                }

                lineContainsText = true;
                if (measurer.getPosition() == tabLocations[currentTab] + 1) {
                    currentTab++;
                }

                if (measurer.getPosition() == styledText.getEndIndex()) {
                    lineComplete = true;
                } else if (tabStops.length == 0 || horizontalPos >= tabStops[tabStops.length - 1]) {
                    lineComplete = true;
                }
                if (!lineComplete) {
                    // move to next tab stop
                    int j = 0;
                    while (horizontalPos >= tabStops[j]) {
                        j++;
                    }
                    horizontalPos = tabStops[j];
                }
            }
            // If there is only one layout element on the line, and we are
            // drawing, then honor alignment
            if (first == layouts.size() - 1) {
                switch (textAlignment) {
                    case RIGHT:
                        penPositions.set(first, rightMargin - layouts.get(first).getVisibleAdvance() - 1);
                        break;
                    case CENTER:
                        //  penPositions.set(first, (rightMargin - 1 - leftMargin - layouts.get(first).getVisibleAdvance()) / 2 + leftMargin);
                        penPositions.set(first, (rightMargin - 1 - leftMargin) * 0.5f + leftMargin);
                        break;
                    case JUSTIFY:
                        // not supported
                        break;
                    case LEFT:
                    default:
                        break;
                }
            }

            verticalPos += maxAscent;
            Iterator<Float> positionEnum = penPositions.iterator();

            // now iterate through layouts and draw them
            styledText.first();
            for (TextLayout nextLayout : layouts) {
                float nextPosition = positionEnum.next();

                w.writeStartElement("tspan");
                int characterCount = nextLayout.getCharacterCount();
                w.writeAttribute("x", nbf.toString(nextPosition));
                w.writeAttribute("y", nbf.toString(verticalPos));
                w.writeCharacters(paragraph.substring(textIndex, textIndex + characterCount));
                w.writeEndElement();

                Rectangle2D layoutBounds = nextLayout.getBounds();
                paragraphBounds.add(new Rectangle2D.Double(layoutBounds.getX() + nextPosition,
                        layoutBounds.getY() + verticalPos,
                        layoutBounds.getWidth(),
                        layoutBounds.getHeight()));

                textIndex += characterCount;
            }

            verticalPos += maxDescent + lineSpacing;
            paragraphBounds.add(paragraphBounds.getX(), verticalPos);
        }

        return paragraphBounds;
    }

    /**
     * Writes text as a series of tspan elements.
     *
     * @param w               the writer
     * @param str             the text
     * @param textRect        the bounding rectangles of the text
     * @param tfont           the font of the text
     * @param tabSize         the tabulator size
     * @param isUnderlined    whether the text is underlined
     * @param isStrikethrough whether the text is striked through
     * @param textAlignment   the alignment of the text
     * @param lineSpacing     the line spacing of the text
     * @throws XMLStreamException on write failure
     */
    private void drawText(XMLStreamWriter w, @Nullable String str,
                          Bounds textRect,
                          Font tfont, int tabSize, boolean isUnderlined,
                          boolean isStrikethrough,
                          TextAlignment textAlignment,
                          double lineSpacing) throws XMLStreamException {
        FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
        java.awt.Font font = new java.awt.Font(tfont.getName(), java.awt.Font.PLAIN, (int) tfont.getSize()).deriveFont((float) tfont.getSize());
        float leftMargin = (float) textRect.getMinX();
        float rightMargin = (float) Math.max(leftMargin, textRect.getMinX() + textRect.getWidth());
        float verticalPos = (float) textRect.getMinY();
        float maxVerticalPos = (float) (textRect.getMinY() + textRect.getHeight());
        if (leftMargin < rightMargin) {
            float tabWidth = (float) (tabSize * font.getStringBounds("m", frc).getWidth());
            float[] tabStops = new float[(int) (textRect.getWidth() / tabWidth)];
            for (int i = 0; i < tabStops.length; i++) {
                tabStops[i] = (float) (textRect.getMinX() + (int) (tabWidth * (i + 1)));
            }

            if (str != null) {
                String[] paragraphs = str.split("\n");

                for (int i = 0; i < paragraphs.length; i++) {
                    if (paragraphs[i].isEmpty()) {
                        paragraphs[i] = " ";
                    }
                    AttributedString as = new AttributedString(paragraphs[i]);
                    as.addAttribute(TextAttribute.FONT, font);
                    if (isUnderlined) {
                        as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
                    }
                    if (isStrikethrough) {
                        as.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.UNDERLINE_LOW_ONE_PIXEL);
                    }
                    int tabCount = paragraphs[i].split("\t").length - 1;
                    Rectangle2D.Double paragraphBounds = drawParagraph(w, frc,
                            paragraphs[i], as.getIterator(), verticalPos, maxVerticalPos, leftMargin, rightMargin, tabStops, tabCount, textAlignment,
                            (float) lineSpacing);
                    verticalPos = (float) (paragraphBounds.y + paragraphBounds.height + lineSpacing);
                    if (verticalPos > maxVerticalPos) {
                        break;
                    }
                }
            }
        }
    }

    protected abstract List<String> getAdditionalNodeClasses(Node node);

    protected abstract String getSvgBaseProfile();

    protected abstract String getSvgVersion();


    private void initIdFactoryRecursively(Node node) {
        String id = node.getId();
        if (id != null && idFactory.getObject(id) == null) {
            idFactory.putIdAndObject(id, node);
        } else {
            idFactory.createId(node, node.getTypeSelector().toLowerCase());
        }

        if (node instanceof Parent pp) {
            for (Node child : pp.getChildrenUnmodifiable()) {
                initIdFactoryRecursively(child);
            }
        }
    }

    public boolean isExportInvisibleElements() {
        return getNonNull(EXPORT_INVISIBLE_ELEMENTS_KEY);
    }

    public void setExportInvisibleElements(boolean newValue) {
        this.setNonNull(EXPORT_INVISIBLE_ELEMENTS_KEY, newValue);
    }

    public boolean isRelativizePaths() {
        return getNonNull(RELATIVIZE_PATHS_KEY);
    }

    public void setRelativizePaths(boolean relativizePaths) {
        this.setNonNull(RELATIVIZE_PATHS_KEY, relativizePaths);
    }


    public boolean isConvertTextToPath() {
        return getNonNull(CONVERT_TEXT_TO_PATH_KEY);
    }

    public void setConvertTextToPath(boolean newValue) {
        this.setNonNull(CONVERT_TEXT_TO_PATH_KEY, newValue);
    }


    private boolean shouldWriteDefs(Node drawingNode) {
        return shouldWriteDefsRecursively(drawingNode);
    }

    private boolean shouldWriteDefsRecursively(Node node) {
        if (shouldWriteNode(node)) {
            if (node.getClip() != null) {
                return true;
            }

            if (node instanceof Shape shape) {
                Paint fill = shape.getFill();
                Paint stroke = shape.getStroke();
                return fill instanceof LinearGradient
                        || fill instanceof RadialGradient
                        || stroke instanceof LinearGradient
                        || stroke instanceof RadialGradient;
            }

            if (node instanceof Parent pp) {
                for (Node child : pp.getChildrenUnmodifiable()) {
                    if (shouldWriteDefsRecursively(child)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean shouldWriteNode(Node node) {
        if (skipKey != null && Objects.equals(Boolean.TRUE, node.getProperties().get(skipKey))) {
            return false;
        }
        if (!isExportInvisibleElements()) {
            if (!node.isVisible()) {
                return false;
            }
            if (node instanceof Shape s) {
                if ((s.getFill() == null || ((s.getFill() instanceof Color) && ((Color) s.getFill()).getOpacity() == 0))
                        && (s.getStroke() == null || ((s.getStroke() instanceof Color) && ((Color) s.getStroke()).getOpacity() == 0))
                ) {
                    return false;
                }
                switch (node) {
                    case Path p -> {
                        return !p.getElements().isEmpty();
                    }
                    case Polyline p -> {
                        return !p.getPoints().isEmpty();
                    }
                    case Polygon p -> {
                        return !p.getPoints().isEmpty();
                    }
                    default -> {
                    }
                }
            } else if (node instanceof Group g) {
                return !g.getChildren().isEmpty();
            }
        }
        return true;
    }

    public Document toDocument(Node drawingNode, @Nullable CssDimension2D size, @Nullable CssRectangle2D viewBox) throws IOException {
        try {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            // We do not want that the builder creates a socket connection!
            builder.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));
            Document doc = builder.newDocument();
            DOMResult result = new DOMResult(doc);
            XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter w = xmlOutputFactory.createXMLStreamWriter(result);
            writeDocument(w, drawingNode, size, viewBox);
            w.close();
            return doc;
        } catch (XMLStreamException | ParserConfigurationException e) {
            throw new IOException("Error writing to DOM.", e);
        }
    }

    public void write(OutputStream out, Node drawingNode, @Nullable CssDimension2D size, @Nullable CssRectangle2D viewBox) throws IOException {
        IndentingXMLStreamWriter w = new IndentingXMLStreamWriter(out);
        try {
            writeDocument(w, drawingNode, size, viewBox);
            w.flush();
        } catch (XMLStreamException e) {
            throw new IOException("Error writing to Writer.", e);
        }
    }

    public void write(Writer out, Node drawingNode, @Nullable CssDimension2D size, @Nullable CssRectangle2D viewBox) throws IOException {
        IndentingXMLStreamWriter w = new IndentingXMLStreamWriter(out);
        try {
            writeDocument(w, drawingNode, size, viewBox);
            w.flush();
        } catch (XMLStreamException e) {
            throw new IOException("Error writing to Writer.", e);
        }
    }

    private void writeArcStartElement(XMLStreamWriter w, Arc node) throws XMLStreamException {
        w.writeStartElement("path");

        StringBuilder buf = new StringBuilder();
        double centerX = node.getCenterX();
        double centerY = node.getCenterY();
        double radiusX = node.getRadiusX();
        double radiusY = node.getRadiusY();
        double startAngle = Math.toRadians(-node.getStartAngle());
        double endAngle = Math.toRadians(-node.getStartAngle() - node.getLength());
        double length = node.getLength();

        double startX = radiusX * Math.cos(startAngle);
        double startY = radiusY * Math.sin(startAngle);

        double endX = centerX + radiusX * Math.cos(endAngle);
        double endY = centerY + radiusY * Math.sin(endAngle);

        int xAxisRot = 0;
        boolean largeArc = (length > 180);
        boolean sweep = (length < 0);

        buf.append('M')
                .append(nb.toString(centerX))
                .append(',')
                .append(nb.toString(centerY))
                .append(' ');

        if (ArcType.ROUND == node.getType()) {
            buf.append('l')
                    .append(startX)
                    .append(',')
                    .append(startY).append(' ');
        }

        buf.append('A')
                .append(nb.toString(radiusX))
                .append(',')
                .append(nb.toString(radiusY))
                .append(',')
                .append(nbi.toString(xAxisRot))
                .append(',')
                .append(largeArc ? '1' : '0')
                .append(',')
                .append(sweep ? '1' : '0')
                .append(',')
                .append(nb.toString(endX))
                .append(',')
                .append(nb.toString(endY))
                .append(',');

        if (ArcType.CHORD == node.getType()
                || ArcType.ROUND == node.getType()) {
            buf.append('Z');
        }
        w.writeAttribute("d", buf.toString());
    }

    private void writeCircleStartElement(XMLStreamWriter w, Circle node) throws XMLStreamException {
        w.writeStartElement("circle");
        if (node.getCenterX() != 0.0) {
            w.writeAttribute("cx", nb.toString(node.getCenterX()));
        }
        if (node.getCenterY() != 0.0) {
            w.writeAttribute("cy", nb.toString(node.getCenterY()));
        }
        if (node.getRadius() != 0.0) {
            w.writeAttribute("r", nb.toString(node.getRadius()));
        }
    }

    private void writeClassAttribute(XMLStreamWriter w, Node node) throws XMLStreamException {
        List<String> styleClass = new ArrayList<>(node.getStyleClass());
        styleClass.addAll(getAdditionalNodeClasses(node));

        if (!styleClass.isEmpty()) {
            StringBuilder buf = new StringBuilder();
            for (String clazz : styleClass) {
                if (!buf.isEmpty()) {
                    buf.append(' ');
                }
                buf.append(clazz);
            }
            w.writeAttribute("class", buf.toString());
        }
    }

    protected abstract void writeClipAttributes(XMLStreamWriter w, Node node) throws XMLStreamException;

    protected abstract void writeClipPathDefs(XMLStreamWriter w, Node node) throws XMLStreamException, IOException;

    protected abstract void writeCompositingAttributes(XMLStreamWriter w, Node
            node) throws XMLStreamException;

    private void writeCubicCurveStartElement(XMLStreamWriter w, CubicCurve node) throws XMLStreamException {
        w.writeStartElement("path");
        final StringBuilder buf = new StringBuilder();
        buf.append('M')
                .append(nb.toString(node.getStartX()))
                .append(',')
                .append(nb.toString(node.getStartY()))
                .append(' ')
                .append('C')
                .append(nb.toString(node.getControlX1()))
                .append(',')
                .append(nb.toString(node.getControlY1()))
                .append(',')
                .append(nb.toString(node.getControlX2()))
                .append(',')
                .append(nb.toString(node.getControlY2()))
                .append(',')
                .append(nb.toString(node.getEndX()))
                .append(',')
                .append(nb.toString(node.getEndY()));
        w.writeAttribute("d", buf.substring(0));
    }

    private void writeDefs(XMLStreamWriter w, Node drawingNode) throws XMLStreamException, IOException {
        w.writeStartElement("defs");
        writeDefsRecursively(w, drawingNode);
        w.writeEndElement();
    }

    private void writeDefsRecursively(XMLStreamWriter w, Node node) throws IOException, XMLStreamException {
        if (!shouldWriteNode(node)) {
            return;
        }

        writeClipPathDefs(w, node);

        if (node instanceof Shape shape) {
            writePaintDefs(w, shape.getFill());
            writePaintDefs(w, shape.getStroke());
        }

        if (node instanceof Parent pp) {
            for (Node child : pp.getChildrenUnmodifiable()) {
                writeDefsRecursively(w, child);
            }
        }

    }

    protected void writeDescElement(XMLStreamWriter w, Node node) throws XMLStreamException {
        Object descObj = node.getProperties().get(DESC_PROPERTY_NAME);
        if ((descObj instanceof String)) {
            String desc = ((String) descObj).trim();
            if (!desc.isEmpty()) {
                w.writeStartElement("desc");
                w.writeCharacters(desc);
                w.writeEndElement();
            }
        }
    }

    private void writeDocument(XMLStreamWriter w, Node drawingNode, @Nullable CssDimension2D size, @Nullable CssRectangle2D viewBox) throws XMLStreamException, IOException {
        idFactory.reset();
        initIdFactoryRecursively(drawingNode);

        w.writeStartDocument();
        w.setDefaultNamespace(SVG_NS);
        w.writeStartElement("svg");
        w.writeDefaultNamespace(SVG_NS);
        w.writeNamespace(XLINK_PREFIX, XLINK_NS);
        writeDocumentElementAttributes(w, drawingNode, size, viewBox);

        if (shouldWriteDefs(drawingNode)) {
            writeDefs(w, drawingNode);
        }
        writeNodeRecursively(w, drawingNode, 1);
        w.writeEndElement();
        w.writeEndDocument();
    }

    protected abstract void writeDocumentElementAttributes(XMLStreamWriter
                                                                   w, Node drawingNode, @Nullable CssDimension2D size, @Nullable CssRectangle2D viewBox) throws XMLStreamException;

    private void writeEllipseStartElement(XMLStreamWriter w, Ellipse node) throws XMLStreamException {
        w.writeStartElement("ellipse");
        if (node.getCenterX() != 0.0) {
            w.writeAttribute("cx", nb.toString(node.getCenterX()));
        }
        if (node.getCenterY() != 0.0) {
            w.writeAttribute("cy", nb.toString(node.getCenterY()));
        }
        if (node.getRadiusX() != 0.0) {
            w.writeAttribute("rx", nb.toString(node.getRadiusX()));
        }
        if (node.getRadiusY() != 0.0) {
            w.writeAttribute("ry", nb.toString(node.getRadiusY()));
        }
    }

    private void writeFillAttributes(XMLStreamWriter w, Shape shape) throws XMLStreamException {
        Paint fill = shape.getFill();
        String id = idFactory.getId(fill);
        double fillOpacity = shape.getOpacity();

        if (id != null) {
            w.writeAttribute("fill", "url(#" + id + ")");
        } else {
            w.writeAttribute("fill", paintConverter.toString(fill));
            if (fill instanceof Color c) {
                if (!c.isOpaque()) {
                    fillOpacity *= c.getOpacity();
                }
            }
        }
        if (fillOpacity != 1) {
            w.writeAttribute("fill-opacity", nb.toString(fillOpacity));
        }

        final FillRule fillRule;
        if (shape instanceof Path path) {
            fillRule = path.getFillRule();
        } else if (shape instanceof SVGPath path) {
            fillRule = path.getFillRule();
        } else {
            fillRule = FillRule.NON_ZERO;
        }
        switch (fillRule) {
            case EVEN_ODD:
                w.writeAttribute("fill-rule", "evenodd");
                break;
            case NON_ZERO:
            default:
                break;
        }
    }

    protected void writeGroupStartElement(XMLStreamWriter w, Group
            node) throws XMLStreamException {
        w.writeStartElement("g");
        writeClipAttributes(w, node);
    }

    protected void writeIdAttribute(XMLStreamWriter w, Node node) throws XMLStreamException {
        String id = node.getId();
        if (id != null && !id.isEmpty()) {
            w.writeAttribute("id", id);
        }
    }

    private void writeImageViewStartElement(XMLStreamWriter w, ImageView
            node) throws IOException, XMLStreamException {
        w.writeStartElement("image");

        w.writeAttribute("x", nb.toString(node.getX()));
        w.writeAttribute("y", nb.toString(node.getY()));
        w.writeAttribute("width", nb.toString(node.getFitWidth()));
        w.writeAttribute("height", nb.toString(node.getFitHeight()));
        w.writeAttribute("preserveAspectRatio", node.isPreserveRatio() ? "xMidYMid" : "none");

        URI uri = (URI) node.getProperties().get(imageUriKey);
        String href = null;
        if (uri != null) {
            href = idFactory.relativize(uri).toString();
        } else {
            if (node.getImage() != null) {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                final BufferedImage im = fromFXImage(node.getImage(), null);
                if (im == null) {
                    throw new IOException("Could not create an AWT image.");
                }
                ImageIO.write(im, "PNG", bout);
                bout.close();
                byte[] imageData = bout.toByteArray();

                href = "data:image;base64," + Base64.getEncoder().encodeToString(imageData);
            }
        }
        if (href != null) {
            w.writeAttribute(XLINK_PREFIX, XLINK_NS, "href", href);
        }
    }

    private void writeLineStartElement(XMLStreamWriter w, Line
            node) throws XMLStreamException {
        w.writeStartElement("line");
        if (node.getStartX() != 0.0) {
            w.writeAttribute("x1", nb.toString(node.getStartX()));
        }
        if (node.getStartY() != 0.0) {
            w.writeAttribute("y1", nb.toString(node.getStartY()));
        }
        if (node.getEndX() != 0.0) {
            w.writeAttribute("x2", nb.toString(node.getEndX()));
        }
        if (node.getEndY() != 0.0) {
            w.writeAttribute("y2", nb.toString(node.getEndY()));
        }

    }

    private void writeLinearGradientDef(XMLStreamWriter w, LinearGradient g) throws IOException, XMLStreamException {
        String id = idFactory.createId(g, "linearGradient");
        w.writeStartElement("linearGradient");

        w.writeAttribute("id", id);
        if (g.isProportional()) {
            w.writeAttribute("x1", nb.toString(g.getStartX() * 100) + "%");
            w.writeAttribute("y1", nb.toString(g.getStartY() * 100) + "%");
            w.writeAttribute("x2", nb.toString(g.getEndX() * 100) + "%");
            w.writeAttribute("y2", nb.toString(g.getEndY() * 100) + "%");
            w.writeAttribute("gradientUnits", "objectBoundingBox");

        } else {
            w.writeAttribute("x1", nb.toString(g.getStartX()));
            w.writeAttribute("y1", nb.toString(g.getStartY()));
            w.writeAttribute("x2", nb.toString(g.getEndX()));
            w.writeAttribute("y2", nb.toString(g.getEndY()));
            w.writeAttribute("gradientUnits", "userSpaceOnUse");
        }
        switch (g.getCycleMethod()) {
            case NO_CYCLE:
                w.writeAttribute("spreadMethod", "pad");
                break;
            case REFLECT:
                w.writeAttribute("spreadMethod", "reflect");
                break;
            case REPEAT:
                w.writeAttribute("spreadMethod", "repeat");
                break;
            default:
                throw new IOException("unsupported cycle method:" + g.getCycleMethod());
        }
        for (Stop s : g.getStops()) {
            w.writeStartElement("stop");
            w.writeAttribute("offset", nb.toString(s.getOffset() * 100) + "%");
            Color c = s.getColor();
            w.writeAttribute("stop-color", this.paintConverter.toString(c));
            if (!c.isOpaque()) {
                w.writeAttribute("stop-opacity", nb.toString(c.getOpacity()));
            }
            w.writeEndElement();
        }
        w.writeEndElement();
    }

    private void writeMetadataChildElements(XMLStreamWriter w, Node node) throws XMLStreamException {
        writeTitleElement(w, node);
        writeDescElement(w, node);
    }

    protected void writeNodeRecursively(XMLStreamWriter w, Node node, int depth) throws IOException, XMLStreamException {
        if (!shouldWriteNode(node)) {
            return;
        }

        switch (node) {
            case Shape shape -> {
                writeShapeStartElement(w, shape);
                writeFillAttributes(w, shape);
                if (shape.getStrokeType() == StrokeType.CENTERED) {
                    writeStrokeAttributes(w, (Shape) node);
                }
                writeClipAttributes(w, node);
            }
            case Group group -> writeGroupStartElement(w, group);
            case Region region -> writeRegionStartElement(w, region);
            case ImageView imageView -> writeImageViewStartElement(w, imageView);
            default -> throw new IOException("not yet implemented for " + node);
        }
        writeStyleAttributes(w, node);
        writeTransformAttributes(w, node);
        writeCompositingAttributes(w, node);
        writeMetadataChildElements(w, node);

        if (node instanceof Shape) {
            writeShapeChildElements(w, (Shape) node);
        } else if (node instanceof Region) {
            writeRegionChildElements(w, (Region) node);
        }

        if (node instanceof Parent pp) {
            for (Node child : pp.getChildrenUnmodifiable()) {
                writeNodeRecursively(w, child, depth + 1);
            }
        }
        w.writeEndElement();

        if (node instanceof Shape && ((Shape) node).getStrokeType() != StrokeType.CENTERED) {
            writeStrokedShapeElement(w, ((Shape) node));
        }
    }

    private void writePaintDefs(XMLStreamWriter w, Paint paint) throws IOException, XMLStreamException {
        if (idFactory.getId(paint) == null) {
            if (paint instanceof LinearGradient g) {
                writeLinearGradientDef(w, g);
            } else if (paint instanceof RadialGradient g) {
                writeRadialGradientDef(w, g);
            }
        }
    }

    protected void writePathStartElement(XMLStreamWriter w, Path node) throws XMLStreamException {
        w.writeStartElement("path");
        String d;
        if (isRelativizePaths()) {
            d = SvgPaths.awtPathIteratorToDoubleRelativeSvgString(FXShapes.fxPathELementsToAwtShape(node.getElements(), node.getFillRule()).getPathIterator(null));
        } else {
            d = FXSvgPaths.pathElementsToDoubleSvgString(node.getElements());
        }
        w.writeAttribute("d", d);
    }

    protected void writeStrokedShapeElement(XMLStreamWriter w, Shape fxShape) throws XMLStreamException, IOException {
        w.writeStartElement("path");

        java.awt.Shape shape = FXShapes.fxShapeToAwtShape(fxShape);
        int cap = switch (fxShape.getStrokeLineCap()) {
            case SQUARE -> BasicStroke.CAP_SQUARE;
            default -> BasicStroke.CAP_BUTT;
            case ROUND -> BasicStroke.CAP_ROUND;
        };
        int join = switch (fxShape.getStrokeLineJoin()) {
            default -> BasicStroke.JOIN_MITER;
            case BEVEL -> BasicStroke.JOIN_BEVEL;
            case ROUND -> BasicStroke.JOIN_ROUND;
        };
        ObservableList<Double> strokeDashArray = fxShape.getStrokeDashArray();
        float[] dashes = new float[strokeDashArray.size()];
        for (int i = 0; i < strokeDashArray.size(); i++) {
            dashes[i] = strokeDashArray.get(i).floatValue();
        }

        java.awt.Shape strokedShape = new BasicStroke(
                (float) (fxShape.getStrokeWidth() * (fxShape.getStrokeType() == StrokeType.CENTERED ? 1 : 2)),
                cap, join,
                (float) fxShape.getStrokeMiterLimit(),
                dashes.length == 0 ? null : dashes, (float) fxShape.getStrokeDashOffset()).createStrokedShape(shape);
        Area area = new Area(strokedShape);

        switch (fxShape.getStrokeType()) {
            case INSIDE:
                area.intersect(new Area(shape));
                break;
            case OUTSIDE:
                area.subtract(new Area(shape));
                break;
            case CENTERED:
                break;
        }

        String d;
        if (isRelativizePaths()) {
            d = SvgPaths.awtPathIteratorToDoubleRelativeSvgString(area.getPathIterator(null));
        } else {
            d = SvgPaths.awtPathIteratorToDoubleSvgString(area.getPathIterator(null));
        }
        w.writeAttribute("d", d);

        writeStrokedShapeAttributes(w, fxShape);
        w.writeEndElement();
    }

    private void writePolygonStartElement(XMLStreamWriter w, Polygon node) throws XMLStreamException {
        w.writeStartElement("polygon");
        StringBuilder buf = new StringBuilder();
        List<Double> ps = node.getPoints();
        for (int i = 0, n = ps.size(); i < n; i += 2) {
            if (i != 0) {
                buf.append(' ');
            }
            buf.append(nb.toString(ps.get(i)))
                    .append(',')
                    .append(nb.toString(ps.get(i + 1)));
        }
        w.writeAttribute("points", buf.toString());

    }

    private void writePolylineStartElement(XMLStreamWriter w, Polyline node) throws XMLStreamException {
        w.writeStartElement("polyline");
        StringBuilder buf = new StringBuilder();
        List<Double> ps = node.getPoints();
        for (int i = 0, n = ps.size(); i < n; i += 2) {
            if (i != 0) {
                buf.append(' ');
            }
            buf.append(nb.toString(ps.get(i)))
                    .append(',')
                    .append(nb.toString(ps.get(i + 1)));
        }
        w.writeAttribute("points", buf.toString());
    }

    private void writeQuadCurveStartElement(XMLStreamWriter w, QuadCurve
            node) throws XMLStreamException {
        w.writeStartElement("path");
        final StringBuilder buf = new StringBuilder();
        buf.append('M')
                .append(nb.toString(node.getStartX()))
                .append(',')
                .append(nb.toString(node.getStartY()))
                .append(' ')
                .append('Q')
                .append(nb.toString(node.getControlX()))
                .append(',')
                .append(nb.toString(node.getControlY()))
                .append(',')
                .append(nb.toString(node.getEndX()))
                .append(',')
                .append(nb.toString(node.getEndY()));
        w.writeAttribute("d", buf.substring(0));
    }

    private void writeRadialGradientDef(XMLStreamWriter w, RadialGradient g) throws IOException, XMLStreamException {
        String id = idFactory.createId(g, "radialGradient");
        w.writeStartElement("radialGradient");
        w.writeAttribute("id", id);
        if (g.isProportional()) {
            w.writeAttribute("cx", nb.toString(g.getCenterX() * 100) + "%");
            w.writeAttribute("cy", nb.toString(g.getCenterY() * 100) + "%");
            w.writeAttribute("r", nb.toString(g.getRadius() * 100) + "%");
            w.writeAttribute("fx", nb.toString((g.getCenterX() + Math.cos(g.getFocusAngle() / 180 * Math.PI) * g.getFocusDistance() * g.getRadius()) * 100) + "%");
            w.writeAttribute("fy", nb.toString((g.getCenterY() + Math.sin(g.getFocusAngle() / 180 * Math.PI) * g.getFocusDistance() * g.getRadius()) * 100) + "%");
            w.writeAttribute("gradientUnits", "objectBoundingBox");

        } else {
            w.writeAttribute("cx", nb.toString(g.getCenterX()));
            w.writeAttribute("cy", nb.toString(g.getCenterY()));
            w.writeAttribute("r", nb.toString(g.getRadius()));
            w.writeAttribute("fx", nb.toString(g.getCenterX() + Math.cos(g.getFocusAngle() / 180 * Math.PI) * g.getFocusDistance() * g.getRadius()));
            w.writeAttribute("fy", nb.toString(g.getCenterY() + Math.sin(g.getFocusAngle() / 180 * Math.PI) * g.getFocusDistance() * g.getRadius()));
            w.writeAttribute("gradientUnits", "userSpaceOnUse");
        }
        switch (g.getCycleMethod()) {
            case NO_CYCLE:
                w.writeAttribute("spreadMethod", "pad");
                break;
            case REFLECT:
                w.writeAttribute("spreadMethod", "reflect");
                break;
            case REPEAT:
                w.writeAttribute("spreadMethod", "repeat");
                break;
            default:
                throw new IOException("unsupported cycle method:" + g.getCycleMethod());
        }
        for (Stop s : g.getStops()) {
            w.writeStartElement("stop");
            w.writeAttribute("offset", nb.toString(s.getOffset() * 100) + "%");
            Color c = s.getColor();
            w.writeAttribute("stop-color", this.paintConverter.toString(c));
            if (!c.isOpaque()) {
                w.writeAttribute("stop-opacity", nb.toString(c.getOpacity()));
            }
            w.writeEndElement();
        }
        w.writeEndElement();
    }

    private void writeRectangleStartElement(XMLStreamWriter w, Rectangle node) throws XMLStreamException {
        if (!node.getStrokeDashArray().isEmpty()) {
            // If the rectangle has a non-empty stroke dasharray, then
            // we must render it as an SVG path, because SVG uses a different
            // rendering algorithm.

            // FIXME convert to path and then return
        }

        w.writeStartElement("rect");
        if (node.getX() != 0.0) {
            w.writeAttribute("x", nb.toString(node.getX()));
        }
        if (node.getY() != 0.0) {
            w.writeAttribute("y", nb.toString(node.getY()));
        }
        if (node.getWidth() != 0.0) {
            w.writeAttribute("width", nb.toString(node.getWidth()));
        }
        if (node.getHeight() != 0.0) {
            w.writeAttribute("height", nb.toString(node.getHeight()));
        }
        if (node.getArcWidth() != 0.0) {
            w.writeAttribute("rx", nb.toString(node.getArcWidth() * 0.5));
        }
        if (node.getArcHeight() != 0.0) {
            w.writeAttribute("ry", nb.toString(node.getArcHeight() * 0.5));
        }
    }

    private void writeRegionStartElement(XMLStreamWriter w, Region region) throws XMLStreamException {
        w.writeStartElement("g");
    }

    private void writeRegionChildElements(XMLStreamWriter w, Region region) throws IOException, XMLStreamException {

        double x = region.getLayoutX();
        double y = region.getLayoutY();
        double width = region.getWidth();
        double height = region.getHeight();

        if ((region.getBackground() != null && !region.getBackground().isEmpty())
                || (region.getBorder() != null && !region.getBorder().isEmpty())) {
            // compute the shape 's' of the region
            Shape s = region.getShape();
            Bounds sb = (s != null) ? s.getLayoutBounds() : null;

            // All BackgroundFills are drawn first, followed by
            // BackgroundImages, BorderStrokes, and finally BorderImages
            if (region.getBackground() != null) {
                for (BackgroundFill bgf : region.getBackground().getFills()) {
                    Paint fill = bgf.getFill() == null ? Color.TRANSPARENT : bgf.getFill();
                    CornerRadii radii = bgf.getRadii() == null ? CornerRadii.EMPTY : bgf.getRadii();
                    Insets insets = bgf.getInsets() == null ? Insets.EMPTY : bgf.getInsets();

                    Shape bgs;
                    if (s != null) {
                        if (region.isScaleShape()) {

                            java.awt.Shape awtShape = FXShapes.fxShapeToAwtShape(s);
                            Transform tx = Transform.translate(-sb.getMinX(), -sb.getMinY());
                            tx = FXTransforms.concat(tx, Transform.translate(x + insets.getLeft(), y + insets.getTop()));
                            tx = FXTransforms.concat(tx, Transform.scale((width - insets.getLeft() - insets.getRight()) / sb.getWidth(), (height - insets.getTop() - insets.getBottom()) / sb.getHeight()));
                            bgs = FXShapes.awtShapeToFXShape(awtShape, tx);
                        } else {
                            bgs = s;
                        }
                    } else if (radii != CornerRadii.EMPTY) {
                        throw new IOException("radii not yet implemented");
                    } else {
                        bgs = new Rectangle(x + insets.getLeft(), y + insets.getTop(), width - insets.getLeft() - insets.getRight(), height - insets.getTop() - insets.getBottom());
                    }
                    bgs.setFill(fill);
                    writeShapeStartElement(w, bgs);
                    writeFillAttributes(w, bgs);
                    w.writeAttribute("stroke", "none");
                    w.writeEndElement();
                }
                if (!region.getBackground().getImages().isEmpty()) {
                    throw new IOException("background image not yet implemented");
                }
            }
            if (region.getBorder() != null) {
                if (region.getBorder().getImages().isEmpty() || s == null) {
                    for (BorderStroke bs : region.getBorder().getStrokes()) {
                        Insets insets = bs.getInsets();
                        CornerRadii radii = bs.getRadii() == null ? CornerRadii.EMPTY : bs.getRadii();

                        Shape bgs;
                        if (s != null) {
                            if (region.isScaleShape()) {
                                java.awt.Shape awtShape = FXShapes.fxShapeToAwtShape(s);

                                Transform tx = Transform.translate(-sb.getMinX(), -sb.getMinY());
                                tx = FXTransforms.concat(tx, Transform.translate(x + insets.getLeft(), y + insets.getTop()));
                                tx = FXTransforms.concat(tx, Transform.scale((width - insets.getLeft() - insets.getRight()) / sb.getWidth(), (height - insets.getTop() - insets.getBottom()) / sb.getHeight()));
                                bgs = FXShapes.awtShapeToFXShape(awtShape, tx);
                            } else {
                                bgs = s;
                            }
                        } else if (radii != CornerRadii.EMPTY) {
                            throw new IOException("radii not yet implemented");
                        } else {
                            bgs = new Rectangle(x + insets.getLeft(), y + insets.getTop(), width - insets.getLeft() - insets.getRight(), height - insets.getTop() - insets.getBottom());
                        }

                        writeShapeStartElement(w, bgs);
                        writeBorderStrokeAttributes(w, bs);
                        w.writeAttribute("fill", "none");
                        w.writeEndElement();
                    }
                }
                if (s != null && !region.getBorder().getImages().isEmpty()) {
                    throw new IOException("border image not yet implemented");
                }
            }
        }
    }

    private void writeSVGPathStartElement(XMLStreamWriter w, SVGPath node) throws XMLStreamException {
        w.writeStartElement("path");
        w.writeAttribute("d", node.getContent());
        switch (node.getFillRule()) {
            case NON_ZERO:
                //    w.writeAttribute("fill-rule","nonzero");// default
                break;
            case EVEN_ODD:
                w.writeAttribute("fill-rule", "evenodd");
                break;
        }
    }

    private void writeShapeChildElements(XMLStreamWriter w, Shape node) throws XMLStreamException {
        if (node instanceof Text) {
            if (!isConvertTextToPath()) {
                writeTextChildElements(w, (Text) node);
            }
        }
    }

    private void writeShapeStartElement(XMLStreamWriter w, Shape node) throws IOException, XMLStreamException {
        switch (node) {
            case Arc arc -> writeArcStartElement(w, arc);
            case Circle circle -> writeCircleStartElement(w, circle);
            case CubicCurve cubicCurve -> writeCubicCurveStartElement(w, cubicCurve);
            case Ellipse ellipse -> writeEllipseStartElement(w, ellipse);
            case Line line -> writeLineStartElement(w, line);
            case Path path -> writePathStartElement(w, path);
            case Polygon polygon -> writePolygonStartElement(w, polygon);
            case Polyline polyline -> writePolylineStartElement(w, polyline);
            case QuadCurve quadCurve -> writeQuadCurveStartElement(w, quadCurve);
            case Rectangle rectangle -> writeRectangleStartElement(w, rectangle);
            case SVGPath svgPath -> writeSVGPathStartElement(w, svgPath);
            case Text text -> writeTextStartElement(w, text);
            case null, default -> throw new IOException("unknown shape type " + node);
        }
    }

    private void writeStrokeAttributes(XMLStreamWriter w, Shape shape) throws XMLStreamException {
        Paint stroke = shape.getStroke();
        if (stroke == null) {
            return;
        }

        double strokeOpacity = shape.getOpacity();

        String id = idFactory.getId(stroke);
        if (id != null) {
            w.writeAttribute("stroke", "url(#" + id + ")");
        } else {
            w.writeAttribute("stroke", paintConverter.toString(stroke));
            if (stroke instanceof Color c) {
                if (!c.isOpaque()) {
                    strokeOpacity *= c.getOpacity();
                }
            }
        }
        if (strokeOpacity != 1) {
            w.writeAttribute("stroke-opacity", nb.toString(strokeOpacity));
        }

        if (shape.getStrokeWidth() != 1) {
            w.writeAttribute("stroke-width", nb.toString(shape.getStrokeWidth()));
        }
        if (shape.getStrokeLineCap() != StrokeLineCap.BUTT) {
            w.writeAttribute("stroke-linecap", shape.getStrokeLineCap().toString().toLowerCase());
        }
        if (shape.getStrokeLineJoin() != StrokeLineJoin.MITER) {
            w.writeAttribute("stroke-linejoin", shape.getStrokeLineJoin().toString().toLowerCase());
        }
        if (shape.getStrokeMiterLimit() != 4) {
            w.writeAttribute("stroke-miterlimit", nb.toString(shape.getStrokeMiterLimit()));
        }
        if (!shape.getStrokeDashArray().isEmpty()) {
            w.writeAttribute("stroke-dasharray", doubleList.toString(VectorList.copyOf(shape.getStrokeDashArray())));
        }
        if (shape.getStrokeDashOffset() != 0) {
            w.writeAttribute("stroke-dashoffset", nb.toString(shape.getStrokeDashOffset()));
        }
        /* XXX We must simulated non-centered strokes because SVG does not support it yet.
        if (shape.getStrokeType() != StrokeType.CENTERED) {
            // XXX this is currentl only a proposal for SVG 2
            //       https://svgwg.org/specs/strokes/#SpecifyingStrokeAlignment
            switch (shape.getStrokeType()) {
            case INSIDE:
                w.writeAttribute("stroke-align", "inner");
                break;
            case CENTERED:
                break;
            case OUTSIDE:
                w.writeAttribute("stroke-align", "outer");
                break;
            default:
                throw new IOException("Unsupported stroke type " + shape.getStrokeType());
            }
        }*/
    }

    private void writeStrokedShapeAttributes(XMLStreamWriter w, Shape shape) throws XMLStreamException {
        Paint stroke = shape.getStroke();
        if (stroke == null) {
            return;
        }


        String id = idFactory.getId(stroke);
        if (id != null) {
            w.writeAttribute("fill", "url(#" + id + ")");
        } else {
            w.writeAttribute("fill", paintConverter.toString(stroke));
            if (stroke instanceof Color c) {
                if (!c.isOpaque()) {
                    w.writeAttribute("fill-opacity", nb.toString(c.getOpacity()));
                }
            }
        }
    }

    private void writeBorderStrokeAttributes(XMLStreamWriter w, BorderStroke shape) throws XMLStreamException {
        if (shape.getTopStroke() != null) {
            w.writeAttribute("stroke", paintConverter.toString(shape.getTopStroke()));
        }
        if (shape.getWidths().getTop() != 1) {
            w.writeAttribute("stroke-width", nb.toString(shape.getWidths().getTop()));
        }
        BorderStrokeStyle style = shape.getTopStyle();
        // FIXME support top/right/bottom/left style!!
        if (style.getLineCap() != StrokeLineCap.BUTT) {
            w.writeAttribute("stroke-linecap", style.getLineCap().toString().toLowerCase());
        }
        if (style.getLineJoin() != StrokeLineJoin.MITER) {
            w.writeAttribute("stroke-linejoin", style.getLineJoin().toString().toLowerCase());
        }
        if (style.getMiterLimit() != 4) {
            w.writeAttribute("stroke-miterlimit", nb.toString(style.getMiterLimit()));
        }
        if (!style.getDashArray().isEmpty()) {
            w.writeAttribute("stroke-dasharray", doubleList.toString(VectorList.copyOf(style.getDashArray())));
        }
        if (style.getDashOffset() != 0) {
            w.writeAttribute("stroke-dashoffset", nb.toString(style.getDashOffset()));
        }
        /* FIXME We must simulate non-centered strokes, because SVG does not support it yet.
        if (style.getType() != StrokeType.CENTERED) {
            // CSS Fill and Stroke Module Level 3
            // https://www.w3.org/TR/fill-stroke-3/#stroke-align
            switch (style.getType()) {
            case INSIDE:
                w.writeAttribute("stroke-align", "inner");
                break;
            case CENTERED:
                break;
            case OUTSIDE:
                w.writeAttribute("stroke-align", "outer");
                break;
            default:
                throw new IOException("Unsupported stroke type " + style.getType());
            }
        }*/
    }

    protected void writeStyleAttributes(XMLStreamWriter w, Node node) throws XMLStreamException {
        writeIdAttribute(w, node);
        writeClassAttribute(w, node);
        writeVisibleAttribute(w, node);
    }

    private void writeTextAttributes(XMLStreamWriter w, Text node) throws XMLStreamException {
        Font ft = node.getFont();
        w.writeAttribute("font-family", (ft.getFamily().equals(ft.getName())) ? "'" + ft.getName() + "'" : "'" + ft.getName() + "', '" + ft.getFamily() + "'");
        w.writeAttribute("font-size", nb.toString(ft.getSize()));
        final String style = ft.getStyle().contains("Italic") ? "italic" : "normal";
        if (!style.equals("normal")) {
            w.writeAttribute("font-style", style);
        }
        final String weight = ft.getStyle().contains("Bold") || ft.getName().toLowerCase().contains("bold") ? "bold" : "normal";
        if (!weight.equals("normal")) {
            w.writeAttribute("font-weight", weight);
        }
        if (node.isUnderline()) {
            w.writeAttribute("text-decoration", "underline");
        } else if (node.isStrikethrough()) {
            w.writeAttribute("text-decoration", "line-through ");
        }
        switch (node.getTextAlignment()) {
            case LEFT, JUSTIFY -> {
            }
            case CENTER -> w.writeAttribute("text-anchor", "middle");
            case RIGHT -> w.writeAttribute("text-anchor", "end");
        }

        if (hasNoWrapping(node)) {
            // If the text has no wrapping width, we can write the
            // text directly into the body of the "text" element.
            Bounds bounds = node.getLayoutBounds();
            switch (node.getTextAlignment()) {
                case LEFT, JUSTIFY -> w.writeAttribute("x", nb.toString(node.getX()));
                case CENTER -> w.writeAttribute("x", nb.toString(node.getX() + bounds.getWidth() * 0.5));
                case RIGHT -> w.writeAttribute("x", nb.toString(node.getX() + bounds.getWidth()));
            }

            // By adding baselineOffset to layoutBounds.minY we get the correct
            // y coordinate for all possible VPos values.
            double y = bounds.getMinY() + node.getBaselineOffset();
            w.writeAttribute("y", nb.toString(y));
        }
    }

    private void writeTextChildElements(XMLStreamWriter w, Text node) throws XMLStreamException {
        double lineSpacing = node.getLineSpacing();//+node.getFont().getSize()*0.15625;
        Bounds textRect = node.getLayoutBounds();
        if (hasNoWrapping(node)) {
            // If the text has no wrapping width, we can write the
            // text directly into the body of the "text" element
            w.writeCharacters(node.getText());
        } else {
            drawText(w, node.getText(), textRect, node.getFont(), 8,
                    node.isUnderline(), node.isStrikethrough(),
                    node.getTextAlignment(), lineSpacing);
        }
    }

    private boolean hasNoWrapping(Text node) {
        String text = node.getText();
        return node.getWrappingWidth() <= 0 && text == null || text.indexOf('\n') < 0;
    }

    private void writeTextStartElement(XMLStreamWriter w, Text node) throws XMLStreamException {
        if (isConvertTextToPath()) {
            w.writeStartElement("path");
            w.writeAttribute("d",
                    SvgPaths.awtPathIteratorToDoubleRelativeSvgString(FXShapes.fxShapeToAwtShape(node).getPathIterator(null)));
        } else {
            w.writeStartElement("text");
            writeTextAttributes(w, node);
        }
    }

    protected void writeTitleElement(XMLStreamWriter w, Node node) throws XMLStreamException {
        Object titleObj = node.getProperties().get(TITLE_PROPERTY_NAME);
        if ((titleObj instanceof String)) {
            String title = ((String) titleObj).trim();
            if (!title.isEmpty()) {
                w.writeStartElement("title");
                w.writeCharacters(title);
                w.writeEndElement();
            }
        }
    }

    private void writeTransformAttributes(XMLStreamWriter w, Node node) throws XMLStreamException {

        // The transforms are applied before translateX, translateY, scaleX,
        // scaleY and rotate transforms.
        List<Transform> txs = new ArrayList<>();
        Point2D pivot = FXRectangles.center(node.getBoundsInLocal());
        if (node.getTranslateX() != 0.0 || node.getTranslateY() != 0.0) {
            txs.add(new Translate(node.getTranslateX(), node.getTranslateY()));
        }
        if (node.getRotate() != 0.0) {
            txs.add(new FXPreciseRotate(node.getRotate(), pivot.getX(), pivot.getY()));
        }
        if (node.getScaleX() != 1.0 || node.getScaleY() != 1.0) {
            txs.add(new Scale(node.getScaleX(), node.getScaleY(), pivot.getX(), pivot.getY()));
        }
        txs.addAll(node.getTransforms());
        writeTransformAttributes(w, txs);
    }

    private void writeTransformAttributes(XMLStreamWriter w, List<Transform> txs) throws XMLStreamException {

        if (!txs.isEmpty()) {
            String value = tx.toString(VectorList.copyOf(txs));
            if (!value.isEmpty()) {
                w.writeAttribute("transform", value);
            }
        }
    }

    private void writeVisibleAttribute(XMLStreamWriter w, Node node) throws XMLStreamException {
        if (!node.isVisible()) {
            //w.writeAttribute("visibility", "hidden");
            w.writeAttribute("display", "none");
        }
    }

}
