/*
 * @(#)SvgExportOutputFormat.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.svg.io;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.DataFormat;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdFactory;
import org.jhotdraw8.base.converter.NumberConverter;
import org.jhotdraw8.base.converter.SimpleIdFactory;
import org.jhotdraw8.css.converter.SizeCssConverter;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.value.CssDimension2D;
import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.ImageFigure;
import org.jhotdraw8.draw.figure.Page;
import org.jhotdraw8.draw.figure.PageFigure;
import org.jhotdraw8.draw.figure.Slice;
import org.jhotdraw8.draw.input.ClipboardOutputFormat;
import org.jhotdraw8.draw.io.AbstractExportOutputFormat;
import org.jhotdraw8.draw.io.OutputFormat;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.draw.render.RenderingIntent;
import org.jhotdraw8.fxbase.concurrent.WorkState;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.NonNullObjectKey;
import org.jhotdraw8.geom.FXTransforms;
import org.jhotdraw8.geom.TransformFlattener;
import org.jhotdraw8.xml.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.logging.Logger;

import static org.jhotdraw8.draw.render.SimpleDrawingRenderer.toNode;

/**
 * Exports a JavaFX scene graph to SVG.
 *
 * @author Werner Randelshofer
 */
public class SvgExportOutputFormat extends AbstractExportOutputFormat
        implements ClipboardOutputFormat, OutputFormat {
    private static final Logger LOGGER = Logger.getLogger(SvgExportOutputFormat.class.getName());

    public static final NonNullObjectKey<Boolean> RELATIVIZE_PATHS = new NonNullObjectKey<>("relativizePaths", Boolean.class, Boolean.FALSE);

    public static final DataFormat SVG_FORMAT;
    public static final String SVG_MIME_TYPE = "image/svg+xml";
    private static final String SKIP_KEY = "skip";

    static {
        DataFormat fmt = DataFormat.lookupMimeType(SVG_MIME_TYPE);
        if (fmt == null) {
            fmt = new DataFormat(SVG_MIME_TYPE);
        }
        SVG_FORMAT = fmt;
    }

    private final NumberConverter nb = new NumberConverter();
    private final SizeCssConverter sc = new SizeCssConverter(false);
    private final Converter<CssSize> sznb = new SizeCssConverter(false);
    private final @NonNull IdFactory idFactory = new SimpleIdFactory();

    private BiFunction<Object, Object, AbstractFXSvgWriter> exporterFactory = FXSvgFullWriter::new;

    public SvgExportOutputFormat() {
    }

    public void setExporterFactory(BiFunction<Object, Object, AbstractFXSvgWriter> exporterFactory) {
        this.exporterFactory = exporterFactory;
    }

    private @NonNull AbstractFXSvgWriter createExporter() {
        AbstractFXSvgWriter exporter = exporterFactory.apply(ImageFigure.IMAGE_URI, SKIP_KEY);
        exporter.setExportInvisibleElements(
                SvgSceneGraphWriter.EXPORT_INVISIBLE_ELEMENTS_KEY.getNonNull(getOptions()));
        return exporter;
    }

    @Override
    protected @NonNull String getExtension() {
        return "svg";
    }

    @Override
    protected boolean isResolutionIndependent() {
        return true;
    }


    private void markNodesOutsideBoundsWithSkip(@NonNull Node node, Bounds sceneBounds) {
        boolean intersects = node.intersects(node.sceneToLocal(sceneBounds));
        if (intersects) {
            node.getProperties().put(SKIP_KEY, false);
            if (node instanceof Parent) {
                Parent parent = (Parent) node;
                for (Node child : parent.getChildrenUnmodifiable()) {
                    markNodesOutsideBoundsWithSkip(child, sceneBounds);
                }
            }
        } else {
            node.getProperties().put(SKIP_KEY, true);
        }
    }


    public Document toDocument(@Nullable URI documentHome, @NonNull Drawing external, @NonNull Collection<Figure> selection) throws IOException {
        Map<Key<?>, Object> hints = new HashMap<>();
        idFactory.setDocumentHome(documentHome);
        RenderContext.RENDERING_INTENT.put(hints, RenderingIntent.EXPORT);
        javafx.scene.Node drawingNode = toNode(external, selection, hints);
        final AbstractFXSvgWriter exporter = createExporter();
        exporter.setRelativizePaths(isRelativizePaths());
        Document doc = exporter.toDocument(drawingNode,
                new CssDimension2D(
                        external.getNonNull(Drawing.WIDTH),
                        external.getNonNull(Drawing.HEIGHT)));
        writeDrawingElementAttributes(doc.getDocumentElement(), external);
        return doc;
    }

    @Override
    public void write(@NonNull Map<DataFormat, Object> clipboard, @NonNull Drawing drawing, @NonNull Collection<Figure> selection) throws IOException {
        idFactory.reset();
        idFactory.setDocumentHome(null);
        StringWriter out = new StringWriter();
        Map<Key<?>, Object> hints = new HashMap<>();
        RenderContext.RENDERING_INTENT.put(hints, RenderingIntent.EXPORT);
        javafx.scene.Node drawingNode = toNode(drawing, selection, hints);
        final AbstractFXSvgWriter exporter = createExporter();
        exporter.setRelativizePaths(isRelativizePaths());
        exporter.write(out, drawingNode,
                new CssDimension2D(
                        drawing.getNonNull(Drawing.WIDTH),
                        drawing.getNonNull(Drawing.HEIGHT)));

        clipboard.put(SVG_FORMAT, out.toString());
    }

    @Override
    public void write(@NonNull Path file, @NonNull Drawing drawing, @NonNull WorkState<Void> workState) throws IOException {
        idFactory.reset();
        idFactory.setDocumentHome(null);
        if (isExportDrawing()) {
            Map<Key<?>, Object> hints = new HashMap<>();
            RenderContext.RENDERING_INTENT.put(hints, RenderingIntent.EXPORT);
            try (OutputStream w = Files.newOutputStream(file)) {
                final AbstractFXSvgWriter exporter = createExporter();
                exporter.setRelativizePaths(isRelativizePaths());
                javafx.scene.Node drawingNode = toNode(drawing, Collections.singletonList(drawing), hints);
                exporter.write(w, drawingNode,
                        new CssDimension2D(drawing.getNonNull(Drawing.WIDTH), drawing.getNonNull(Drawing.HEIGHT)));
            }
        }
        if (isExportSlices()) {
            writeSlices(file.getParent(), drawing);
        }
        if (isExportPages()) {
            String basename = file.getFileName().toString();
            int p = basename.lastIndexOf('.');
            if (p != -1) {
                basename = basename.substring(0, p);
            }
            writePages(file.getParent(), basename, drawing);
        }
    }

    @Override
    public void write(@NonNull OutputStream out, @Nullable URI documentHome, @NonNull Drawing drawing, @NonNull WorkState<Void> workState) throws IOException {
        write(documentHome, out, drawing, drawing.getChildren());
    }

    protected void write(@Nullable URI documentHome, @NonNull OutputStream out, @NonNull Drawing drawing, @NonNull Collection<Figure> selection) throws IOException {
        Map<Key<?>, Object> hints = new HashMap<>();
        idFactory.reset();
        idFactory.setDocumentHome(documentHome);
        RenderContext.RENDERING_INTENT.put(hints, RenderingIntent.EXPORT);
        Node drawingNode = toNode(drawing, selection, hints);
        final AbstractFXSvgWriter exporter = createExporter();
        exporter.setRelativizePaths(isRelativizePaths());
        exporter.write(out, drawingNode,
                new CssDimension2D(
                        drawing.getNonNull(Drawing.WIDTH),
                        drawing.getNonNull(Drawing.HEIGHT)));
    }

    private boolean isRelativizePaths() {
        return RELATIVIZE_PATHS.getNonNull(getOptions());
    }

    private void writeDrawingElementAttributes(@NonNull Element docElement, @NonNull Drawing drawing) throws IOException {
        docElement.setAttribute("width", sc.toString(drawing.get(Drawing.WIDTH)));
        docElement.setAttribute("height", sc.toString(drawing.get(Drawing.HEIGHT)));
    }

    @Override
    protected void writePage(@NonNull Path file, @NonNull Page page, @NonNull Node node, int pageCount, int pageNumber, int internalPageNumber) throws IOException {
        CssSize pw = page.getNonNull(PageFigure.PAPER_WIDTH);
        CssSize ph = page.getNonNull(PageFigure.PAPER_HEIGHT);
        markNodesOutsideBoundsWithSkip(node, FXTransforms.transform(page.getLocalToWorld(), page.getPageBounds(internalPageNumber)));
        node.getTransforms().setAll(page.getWorldToLocal());
        final AbstractFXSvgWriter exporter = createExporter();
        final Document doc = exporter.toDocument(node, new CssDimension2D(pw, ph));
        writePageElementAttributes(doc.getDocumentElement(), page, internalPageNumber);
        node.getTransforms().clear();
        XmlUtil.write(file, doc);
    }

    private void writePageElementAttributes(@NonNull Element docElement, @NonNull Page page, int internalPageNumber) throws IOException {
        Bounds pb = page.getPageBounds(internalPageNumber);
        docElement.setAttribute("width", sznb.toString(page.get(PageFigure.PAPER_WIDTH)));
        docElement.setAttribute("height", sznb.toString(page.get(PageFigure.PAPER_HEIGHT)));
        docElement.setAttribute("viewBox", nb.
                toString(pb.getMinX()) + " " + nb.toString(pb.getMinY())
                + " " + nb.toString(pb.getWidth()) + " " + nb.toString(pb.getHeight()));
    }

    @Override
    protected boolean writeSlice(@NonNull Path file, @NonNull Slice slice, @NonNull Node node, double dpi) throws IOException {
        LOGGER.info("Writing slice " + file);
        markNodesOutsideBoundsWithSkip(node, slice.getLayoutBounds());
        Transform worldToLocal = slice.getWorldToLocal();
        Point2D sliceOrigin = slice.getSliceOrigin();
        worldToLocal = FXTransforms.concat(worldToLocal, new Translate(-sliceOrigin.getX(), -sliceOrigin.getY()));
        if (!worldToLocal.isIdentity()) {
            node.getTransforms().setAll(worldToLocal);
        }
        new TransformFlattener().flattenTranslates(node);
        final AbstractFXSvgWriter exporter = createExporter();
        Bounds bounds = slice.getBoundsInLocal();
        final Document doc = exporter.toDocument(node, new CssDimension2D(bounds.getWidth(), bounds.getHeight()));
        writeSliceElementAttributes(doc.getDocumentElement(), slice);
        node.getTransforms().clear();
        XmlUtil.write(file, doc);
        return true;
    }

    private void writeSliceElementAttributes(@NonNull Element docElement, @NonNull Slice slice) throws IOException {
        Bounds b = slice.getLayoutBounds();
        Point2D sliceOrigin = slice.getSliceOrigin();
        Transform tx = slice.getWorldToLocal();
        docElement.setAttribute("width", nb.toString(b.getWidth()));
        docElement.setAttribute("height", nb.toString(b.getHeight()));
        docElement.setAttribute("viewBox", nb.
                toString(b.getMinX() - sliceOrigin.getX()) + " " + nb.toString(b.getMinY() - sliceOrigin.getY())
                + " " + nb.toString(b.getWidth()) + " " + nb.toString(b.getHeight()));
    }
}
