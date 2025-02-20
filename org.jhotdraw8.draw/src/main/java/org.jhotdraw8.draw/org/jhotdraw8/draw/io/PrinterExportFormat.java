/*
 * @(#)PrinterExportFormat.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.io;

import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.PrinterJob;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.jhotdraw8.css.value.CssDimension2D;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.DefaultUnitConverter;
import org.jhotdraw8.css.value.UnitConverter;
import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.Page;
import org.jhotdraw8.draw.figure.PageFigure;
import org.jhotdraw8.draw.figure.Slice;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.draw.render.RenderingIntent;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jspecify.annotations.Nullable;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.abs;
import static org.jhotdraw8.draw.render.SimpleDrawingRenderer.toNode;

/**
 * PrinterExportFormat.
 *
 */
public class PrinterExportFormat extends AbstractExportOutputFormat {

    private static final double INCH_2_MM = 25.4;

    public PrinterExportFormat() {
    }

    @Override
    protected String getExtension() {
        return "png";
    }

    @Override
    protected boolean isResolutionIndependent() {
        return false;
    }

    public Paper findPaper(CssDimension2D paperSize) {
        UnitConverter uc = new DefaultUnitConverter(72.0);
        double w = uc.convert(paperSize.getWidth(), UnitConverter.POINTS);
        double h = uc.convert(paperSize.getHeight(), UnitConverter.POINTS);
        for (Paper paper : job.getPrinter().getPrinterAttributes().getSupportedPapers()) {

            if (abs(paper.getWidth() - w) < 1 && abs(paper.getHeight() - h) < 1
                    || abs(paper.getWidth() - h) < 1 && abs(paper.getHeight() - w) < 1) {
                return paper;
            }
        }
        return Paper.A4;
    }

    /**
     * Prints a slice of a drawing.
     *
     * @param pageSize       the page size
     * @param worldToLocal   The worldToLocal transform of the viewport (in case the viewport is rotated)
     * @param viewportBounds the bounds of the viewport that we want to print
     * @param node           the rendered node of the slice
     */
    private void printSlice(CssDimension2D pageSize, @Nullable Transform worldToLocal, Bounds viewportBounds, Node node) {
        Paper paper = findPaper(pageSize);
        Dimension2D pgSize = pageSize.getConvertedValue();
        PageLayout pl = job.getPrinter().createPageLayout(paper, pgSize.getWidth() <= pgSize.getHeight() ? PageOrientation.PORTRAIT : PageOrientation.LANDSCAPE, 0, 0, 0, 0);
        job.getJobSettings().setPageLayout(pl);
        paper = pl.getPaper();
        if (paper == null) {
            paper = Paper.A4;
        }
        double pw, ph;
        pw = paper.getWidth();
        ph = paper.getHeight();
        if (pl.getPageOrientation() == PageOrientation.LANDSCAPE) {
            double swap = pw;
            pw = ph;
            ph = swap;
        }
        double paperAspect = pw / ph;

        double vw = pgSize.getWidth();
        double vh = pgSize.getHeight();
        double viewAspect = vw / vh;
        double scaleFactor;
        if (paperAspect < viewAspect) {
            scaleFactor = pw / vw;
        } else {
            scaleFactor = ph / vh;
        }

        Group oldParent = (node.getParent() instanceof Group) ? (Group) node.getParent() : null;
        int index = -1;
        if (oldParent != null) {
            index = oldParent.getChildren().indexOf(node);
            oldParent.getChildren().remove(index);
        }
        Group printParent = new Group();

        printParent.getChildren().add(node);

        printParent.getTransforms().addAll(
                new Translate(-pl.getLeftMargin(), -pl.getTopMargin()),
                new Scale(scaleFactor, scaleFactor),
                new Translate(-viewportBounds.getMinX(), -viewportBounds.getMinY())
        );
        if (worldToLocal != null) {
            printParent.getTransforms().add(worldToLocal);
        }

        Group printNode = new Group();
        printNode.getChildren().addAll(printParent);
        job.printPage(printNode);
        printParent.getChildren().clear();
        if (oldParent != null) {
            oldParent.getChildren().add(index, node);
        }

    }

    private void setDPI(IIOMetadata metadata, double dpi) throws IIOInvalidTreeException {
        double dotsPerMilli = dpi / INCH_2_MM;

        IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
        horiz.setAttribute("value", Double.toString(dotsPerMilli));

        IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
        vert.setAttribute("value", Double.toString(dotsPerMilli));

        IIOMetadataNode dim = new IIOMetadataNode("CssSize");
        dim.appendChild(horiz);
        dim.appendChild(vert);

        IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
        root.appendChild(dim);

        metadata.mergeTree("javax_imageio_1.0", root);
    }

    @Override
    protected void writePage(Path file, Page page, Node node, int pageCount, int pageNumber, int internalPageNumber) throws IOException {
        CssSize pw = page.get(PageFigure.PAPER_WIDTH);
        double paperWidth = pw.getConvertedValue();
        final Bounds pageBounds = page.getPageBounds(internalPageNumber);
        double factor = paperWidth / pageBounds.getWidth();

        printSlice(page.get(PageFigure.PAPER_SIZE), page.getWorldToLocal(), pageBounds, node);
    }

    @Override
    protected boolean writeSlice(Path file, Slice slice, Node node, double dpi) throws IOException {
        printSlice(null, slice.getWorldToLocal(), slice.getLayoutBounds(), node);
        return false;
    }

    private PrinterJob job;

    public void print(PrinterJob job, Drawing drawing) throws IOException {
        this.job = job;
        try {
            // If we have pages, print the pages, otherwise print the entire drawing
            boolean hasPages = false;
            for (Figure f : drawing.preorderIterable()) {
                if (f instanceof Page) {
                    hasPages = true;
                    break;
                }
            }
            if (hasPages) {
                writePages(null, null, drawing);
            } else {
                writeDrawing(drawing);
            }
        } finally {
            job = null;
        }
    }

    private void writeDrawing(Drawing drawing) throws IOException {
        Map<Key<?>, Object> hints = new HashMap<>();
        Double dpi = EXPORT_PAGES_DPI_KEY.get(getOptions());
        RenderContext.RENDERING_INTENT.put(hints, RenderingIntent.EXPORT);
        RenderContext.DPI.put(hints, dpi);
        Bounds b = drawing.getLayoutBounds();
        Node node = toNode(drawing, Collections.singleton(drawing), hints);

        printSlice(new CssDimension2D(b.getWidth(), b.getHeight()), null, b, node);
    }
}
