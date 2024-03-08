/*
 * @(#)BitmapExportOutputFormat.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.io;

import javafx.application.Platform;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.input.DataFormat;
import javafx.scene.transform.Transform;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.draw.css.value.NamedCssColor;
import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.Page;
import org.jhotdraw8.draw.figure.PageFigure;
import org.jhotdraw8.draw.figure.Slice;
import org.jhotdraw8.draw.input.ClipboardOutputFormat;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.draw.render.RenderingIntent;
import org.jhotdraw8.fxbase.concurrent.WorkState;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.geom.FXTransforms;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.jhotdraw8.draw.render.SimpleDrawingRenderer.toNode;

/**
 * BitmapExportOutputFormat.
 *
 * @author Werner Randelshofer
 */
public class BitmapExportOutputFormat extends AbstractExportOutputFormat implements ClipboardOutputFormat, OutputFormat {

    private static final double INCH_2_MM = 25.4;
    public static final String JPEG_MIME_TYPE = "image/jpeg";
    public static final String PNG_MIME_TYPE = "image/png";

    public BitmapExportOutputFormat() {
    }

    private WritableImage doRenderImage(@NonNull Figure slice, @NonNull Node node, @NonNull Bounds bounds, double dpi) {
        SnapshotParameters parameters = new SnapshotParameters();
        double scale = dpi / RenderContext.DPI.getDefaultValueNonNull();
        parameters.setTransform(FXTransforms.concat(Transform.scale(scale, scale), slice.getWorldToLocal()));
        Drawing drawing = (slice instanceof Drawing) ? (Drawing) slice : slice.getDrawing();
        final CssColor color = drawing != null ? drawing.get(Drawing.BACKGROUND) : NamedCssColor.WHITE;
        if (color != null) {
            parameters.setFill(color.getColor());
        }
        double x = bounds.getMinX() * scale;
        double y = bounds.getMinY() * scale;
        double width = bounds.getWidth() * scale;
        double height = bounds.getHeight() * scale;

        parameters.setViewport(new Rectangle2D(x, y, width, height));

        return node.snapshot(parameters, null);
    }

    @Override
    protected @NonNull String getExtension() {
        return "png";
    }

    @Override
    protected boolean isResolutionIndependent() {
        return false;
    }

    private WritableImage renderImage(@NonNull Drawing drawing, @NonNull Collection<Figure> selection, double dpi) throws IOException {
        Map<Key<?>, Object> hints = new HashMap<>();
        RenderContext.RENDERING_INTENT.put(hints, RenderingIntent.EXPORT);
        RenderContext.DPI.put(hints, dpi);
        Node node = toNode(drawing, selection, hints);
        Bounds selectionBounds = Figure.visualBounds(selection);
        Bounds bounds = selectionBounds == null ? new BoundingBox(0, 0, 640, 480) : selectionBounds;

        return renderImageOnApplicationThread(drawing, dpi, node, bounds);
    }

    private WritableImage renderImageOnApplicationThread(@NonNull Figure figure, double dpi, Node node, Bounds bounds) throws IOException {
        if (!Platform.isFxApplicationThread()) {
            CompletableFuture<WritableImage> future = CompletableFuture.supplyAsync(() -> doRenderImage(figure, node, bounds,
                    dpi), Platform::runLater);
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException ex) {
                throw new IOException(ex);
            }
        } else {
            return doRenderImage(figure, node, bounds, dpi);
        }
    }

    private void setDPI(@NonNull IIOMetadata metadata, double dpi) throws IIOInvalidTreeException {
        double dotsPerMilli = dpi / INCH_2_MM;

        IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
        horiz.setAttribute("value", Double.toString(dotsPerMilli));

        IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
        vert.setAttribute("value", Double.toString(dotsPerMilli));

        IIOMetadataNode dim = new IIOMetadataNode("Dimension");
        dim.appendChild(horiz);
        dim.appendChild(vert);

        IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
        root.appendChild(dim);

        metadata.mergeTree("javax_imageio_1.0", root);
    }

    @Override
    public void write(@NonNull Map<DataFormat, Object> out, @NonNull Drawing drawing, @NonNull Collection<Figure> selection) throws IOException {
        WritableImage image = renderImage(drawing, selection, EXPORT_DRAWING_DPI_KEY.getNonNull(getOptions()));
        out.put(DataFormat.IMAGE, image);
    }

    @Override
    public void write(@NonNull OutputStream out, @Nullable URI documentHome, @NonNull Drawing drawing, @NonNull WorkState<Void> workState) throws IOException {
        WritableImage writableImage = renderImage(drawing, Collections.singleton(drawing), EXPORT_DRAWING_DPI_KEY.getNonNull(getOptions()));
        //ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", out);
        writeImage(out, writableImage, EXPORT_DRAWING_DPI_KEY.getNonNull(getOptions()));

    }

    @Override
    public void write(@NonNull Path file, @NonNull Drawing drawing, @NonNull WorkState<Void> workState) throws IOException {
        if (isExportDrawing()) {
            OutputFormat.super.write(file, drawing, workState);
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

    private void writeImage(@NonNull OutputStream out, @NonNull WritableImage writableImage, double dpi) throws IOException {
        BufferedImage image = fromFXImage(writableImage, null);
        if (image == null) {
            throw new IOException("Could not convert the JavaFX image to AWT.");
        }

        for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName("png"); iw.hasNext(); ) {
            ImageWriter writer = iw.next();
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
            IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
            if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
                continue;
            }

            setDPI(metadata, dpi);

            try (ImageOutputStream output = new MemoryCacheImageOutputStream(out)) {
                writer.setOutput(output);
                writer.write(metadata, new IIOImage(image, null, metadata), writeParam);
            }
            break;
        }
    }

    @Override
    protected void writePage(@NonNull Path file, @NonNull Page page, @NonNull Node node, int pageCount, int pageNumber, int internalPageNumber) throws IOException {
        CssSize pw = page.getNonNull(PageFigure.PAPER_WIDTH);
        double paperWidth = pw.getConvertedValue();
        final Bounds pageBounds = page.getPageBounds(internalPageNumber);
        double factor = paperWidth / pageBounds.getWidth();
        final double dpi = EXPORT_PAGES_DPI_KEY.getNonNull(getOptions());
        WritableImage image = renderImageOnApplicationThread(page, dpi * factor, node, pageBounds);
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(file))) {
            writeImage(out, image, dpi);
        }
    }

    @Override
    protected boolean writeSlice(@NonNull Path file, @NonNull Slice slice, @NonNull Node node, double dpi) throws IOException {
        WritableImage image = renderImageOnApplicationThread(slice, dpi, node, slice.getLayoutBounds());
        try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(file))) {
            writeImage(out, image, dpi);
        }
        return false;
    }

    /**
     * Snapshots the specified JavaFX {@link Image} object and stores a
     * copy of its pixels into a {@link BufferedImage} object, creating
     * a new object if needed.
     * The method will only convert a JavaFX {@code Image} that is readable
     * as per the conditions on the
     * {@link Image#getPixelReader() Image.getPixelReader()}
     * method.
     * If the {@code Image} is not readable, as determined by its
     * {@code getPixelReader()} method, then this method will return null.
     * If the {@code Image} is a writable, or other dynamic image, then
     * the {@code BufferedImage} will only be set to the current state of
     * the pixels in the image as determined by its {@link PixelReader}.
     * Further changes to the pixels of the {@code Image} will not be
     * reflected in the returned {@code BufferedImage}.
     * <p>
     * The optional {@code BufferedImage} parameter may be reused to store
     * the copy of the pixels.
     * A new {@code BufferedImage} will be created if the supplied object
     * is null, is too small or of a type which the image pixels cannot
     * be easily converted into.
     *
     * @param img  the JavaFX {@code Image} to be converted
     * @param bimg an optional {@code BufferedImage} object that may be
     *             used to store the returned pixel data
     * @return a {@code BufferedImage} containing a snapshot of the JavaFX
     * {@code Image}, or null if the {@code Image} is not readable.
     */
    public static @Nullable BufferedImage fromFXImage(@NonNull Image img, @Nullable BufferedImage bimg) {
        // This method has been copied from class SwingFXUtils.

        PixelReader pr = img.getPixelReader();
        if (pr == null) {
            return null;
        }
        int iw = (int) img.getWidth();
        int ih = (int) img.getHeight();
        PixelFormat<?> fxFormat = pr.getPixelFormat();
        boolean srcPixelsAreOpaque = isSrcPixelsAreOpaque(bimg, pr, iw, ih, fxFormat);
        int prefBimgType = getBestBufferedImageType(pr.getPixelFormat(), bimg, srcPixelsAreOpaque);
        if (bimg != null) {
            int bw = bimg.getWidth();
            int bh = bimg.getHeight();
            if (bw < iw || bh < ih || bimg.getType() != prefBimgType) {
                bimg = null;
            } else if (iw < bw || ih < bh) {
                Graphics2D g2d = bimg.createGraphics();
                g2d.setComposite(AlphaComposite.Clear);
                g2d.fillRect(0, 0, bw, bh);
                g2d.dispose();
            }
        }
        if (bimg == null) {
            bimg = new BufferedImage(iw, ih, prefBimgType);
        }
        DataBufferInt db = (DataBufferInt) bimg.getRaster().getDataBuffer();
        int[] data = db.getData();
        int offset = bimg.getRaster().getDataBuffer().getOffset();
        int scan = 0;
        SampleModel sm = bimg.getRaster().getSampleModel();
        if (sm instanceof SinglePixelPackedSampleModel) {
            scan = ((SinglePixelPackedSampleModel) sm).getScanlineStride();
        }

        WritablePixelFormat<IntBuffer> pf = getAssociatedPixelFormat(bimg);
        pr.getPixels(0, 0, iw, ih, pf, data, offset, scan);
        return bimg;
    }

    private static boolean isSrcPixelsAreOpaque(BufferedImage bimg, PixelReader pr, int iw, int ih, PixelFormat<?> fxFormat) {
        boolean srcPixelsAreOpaque = false;
        switch (fxFormat.getType()) {
            case INT_ARGB_PRE:
            case INT_ARGB:
            case BYTE_BGRA_PRE:
            case BYTE_BGRA:
                // Check fx image opacity only if
                // supplied BufferedImage is without alpha channel
                if (bimg != null &&
                        (bimg.getType() == BufferedImage.TYPE_INT_BGR ||
                                bimg.getType() == BufferedImage.TYPE_INT_RGB)) {
                    srcPixelsAreOpaque = checkFXImageOpaque(pr, iw, ih);
                }
                break;
            case BYTE_RGB:
                srcPixelsAreOpaque = true;
                break;
        }
        return srcPixelsAreOpaque;
    }

    //
    private static boolean checkFXImageOpaque(PixelReader pr, int iw, int ih) {
        for (int y = 0; y < ih; y++) {
            for (int x = 0; x < iw; x++) {
                int argb = pr.getArgb(x, y);
                if ((argb & 0xff000000) != 0xff000000) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Determine the optimal BufferedImage type to use for the specified
     * {@code fxFormat} allowing for the specified {@code bimg} to be used
     * as a potential default storage space if it is not null and is compatible.
     *
     * @param fxFormat the PixelFormat of the source FX Image
     * @param bimg     an optional existing {@code BufferedImage} to be used
     *                 for storage if it is compatible, or null
     * @return the best image type
     */
    static int getBestBufferedImageType(@NonNull PixelFormat<?> fxFormat,
                                        @Nullable BufferedImage bimg,
                                        boolean isOpaque) {
        // This method has been copied from class SwingFXUtils.
        if (bimg != null) {
            int bimgType = bimg.getType();
            if (bimgType == BufferedImage.TYPE_INT_ARGB ||
                    bimgType == BufferedImage.TYPE_INT_ARGB_PRE ||
                    (isOpaque &&
                            (bimgType == BufferedImage.TYPE_INT_BGR ||
                                    bimgType == BufferedImage.TYPE_INT_RGB))) {
                // We will allow the caller to give us a BufferedImage
                // that has an alpha channel, but we might not otherwise
                // construct one ourselves.
                // We will also allow them to choose their own premultiply
                // type which may not match the image.
                // If left to our own devices we might choose a more specific
                // format as indicated by the choices below.
                return bimgType;
            }
        }
        return switch (fxFormat.getType()) {
            default -> BufferedImage.TYPE_INT_ARGB_PRE;
            case BYTE_BGRA, INT_ARGB -> BufferedImage.TYPE_INT_ARGB;
            case BYTE_RGB -> BufferedImage.TYPE_INT_RGB;
            case BYTE_INDEXED -> (fxFormat.isPremultiplied()
                    ? BufferedImage.TYPE_INT_ARGB_PRE
                    : BufferedImage.TYPE_INT_ARGB);
        };
    }

    /**
     * Determine the appropriate {@link WritablePixelFormat} type that can
     * be used to transfer data into the indicated BufferedImage.
     *
     * @param bimg the BufferedImage that will be used as a destination for
     *             a {@code PixelReader<IntBuffer>#getPixels()} operation.
     * @return the pixel format
     */
    private static @NonNull WritablePixelFormat<IntBuffer>
    getAssociatedPixelFormat(@NonNull BufferedImage bimg) {
        // This method has been copied from class SwingFXUtils.
        // Should not happen...
        return switch (bimg.getType()) {
            // We lie here for xRGB, but we vetted that the src data was opaque
            // so we can ignore the alpha.  We use ArgbPre instead of Argb
            // just to get a loop that does not have divides in it if the
            // PixelReader happens to not know the data is opaque.
            case BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_ARGB_PRE -> PixelFormat.getIntArgbPreInstance();
            case BufferedImage.TYPE_INT_ARGB -> PixelFormat.getIntArgbInstance();
            default -> throw new InternalError("Failed to validate BufferedImage type");
        };
    }
}
