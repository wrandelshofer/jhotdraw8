/*
 * @(#)ChromaticityDiagram.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.examples.colorspace;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.transform.Transform;
import org.jhotdraw8.base.concurrent.RangeTask;
import org.jhotdraw8.color.DisplayP3ColorSpace;
import org.jhotdraw8.color.NamedColorSpace;
import org.jhotdraw8.color.RgbBitConverters;
import org.jhotdraw8.color.SrgbColorSpace;
import org.jspecify.annotations.Nullable;

import java.awt.color.ColorSpace;
import java.awt.geom.Path2D;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;


/**
 * Draws a chromacity diagram.
 * <pre>
 * 0.9 ^
 *     |
 *  y  |
 *     |
 *   0 +----------------->
 *     0       x        0.8
 * </pre>
 */
public class ChromaticityDiagram extends Pane {
    private static final float xwMax = 0.8f;
    private static final float xwMin = 0f;
    private static final float xwExtent = xwMax - xwMin;
    private static final float ywMax = 0.9f;
    private static final float ywMin = 0f;
    private static final float ywExtent = ywMax - ywMin;
    private final ObjectProperty<NamedColorSpace> colorSpace = new SimpleObjectProperty<>(new SrgbColorSpace());
    private final ObjectProperty<NamedColorSpace> displayColorSpace = new SimpleObjectProperty<>(new DisplayP3ColorSpace());

    private final ImageView imageView = new ImageView();
    private final Group overlay = new Group();
    @Nullable
    protected PixelBuffer<IntBuffer> pixelBuffer;

    @SuppressWarnings("this-escape")
    public ChromaticityDiagram() {
        getChildren().addAll(
                imageView,
                overlay);
        InvalidationListener invalidationListener = e -> updateImage();
        colorSpaceProperty().addListener(invalidationListener);
        displayColorSpaceProperty().addListener(invalidationListener);
    }

    @Override
    protected void layoutChildren() {
        recreateImage();
        recreateOverlay();
    }

    private void recreateImage() {
        int width = Math.max(1, (int) getWidth());
        int height = Math.max(1, (int) getHeight());

        boolean needsUpdate = false;
        if (pixelBuffer == null
                || pixelBuffer.getWidth() != width
                || pixelBuffer.getHeight() != height) {
            IntBuffer intBuffer = IntBuffer.allocate(width * height);
            PixelFormat<IntBuffer> pixelFormat = PixelFormat.getIntArgbPreInstance();
            pixelBuffer = new PixelBuffer<>(width, height, intBuffer, pixelFormat);
            Image img = new WritableImage(pixelBuffer);
            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            imageView.setViewport(new Rectangle2D(0, 0, width, height));
            imageView.setImage(img);
            needsUpdate = true;
        }
        if (needsUpdate) {
            updateImage();
        }
    }

    private void recreateOverlay() {
        int width = Math.max(1, (int) getWidth());
        int height = Math.max(1, (int) getHeight());

        Path p = new Path();
        p.setFill(null);
        p.setStroke(Color.BLACK);
        ObservableList<PathElement> pe = p.getElements();
        Transform t =
                Transform.translate(0, height).createConcatenation(Transform.scale(width / xwExtent, -height / ywExtent));
        // vertical grid
        for (int wx10 = 0; wx10 < xwMax * 10; wx10++) {
            float wx = wx10 / 10f;
            pe.add(new MoveTo(width * wx / xwExtent, 0));
            pe.add(new LineTo(width * wx / xwExtent, height));
        }

        // horizontal grid
        for (int wy10 = 0; wy10 < ywMax * 10; wy10++) {
            float wy = wy10 / 10f;
            pe.add(new MoveTo(0, height * wy / ywExtent));
            pe.add(new LineTo(width, height * wy / ywExtent));
        }

        // stimuli
        for (Point2D s : getStimuli(getColorSpace())) {
            Point2D pp = t.transform(s);
            pe.add(new MoveTo(pp.getX() - 1, pp.getY() - 1));
            pe.add(new LineTo(pp.getX() + 1, pp.getY() - 1));
            pe.add(new LineTo(pp.getX() + 1, pp.getY() + 1));
            pe.add(new LineTo(pp.getX() - 1, pp.getY() + 1));
            pe.add(new ClosePath());
        }


        overlay.getChildren().setAll(p);
    }

    private List<Point2D> getStimuli(NamedColorSpace cs) {
        var list = new ArrayList<Point2D>();
        if (cs.getType() == ColorSpace.TYPE_RGB) {
            float[] XYZ = new float[3];
            float[] xyY = new float[3];
            convertXYZtoChromacity(cs.toCIEXYZ(new float[]{1, 0, 0}, XYZ), xyY);//red
            list.add(new Point2D(xyY[0], xyY[1]));
            convertXYZtoChromacity(cs.toCIEXYZ(new float[]{0, 1, 0}, XYZ), xyY);//green
            list.add(new Point2D(xyY[0], xyY[1]));
            convertXYZtoChromacity(cs.toCIEXYZ(new float[]{0, 0, 1}, XYZ), xyY);//blue
            list.add(new Point2D(xyY[0], xyY[1]));
        } else if (cs.getType() == ColorSpace.TYPE_CMYK) {
            float[] XYZ = new float[3];
            float[] xyY = new float[3];
            convertXYZtoChromacity(cs.toCIEXYZ(new float[]{1, 0, 0, 0}, XYZ), xyY);//cyan
            list.add(new Point2D(xyY[0], xyY[1]));
            convertXYZtoChromacity(cs.toCIEXYZ(new float[]{0, 1, 0, 0}, XYZ), xyY);//yellow
            list.add(new Point2D(xyY[0], xyY[1]));
            convertXYZtoChromacity(cs.toCIEXYZ(new float[]{0, 0, 1, 0}, XYZ), xyY);//magenta
            list.add(new Point2D(xyY[0], xyY[1]));
        } else {
            list.add(new Point2D(0, 0));
            list.add(new Point2D(0.8, 0.0));
            list.add(new Point2D(0.8, 0.9));
            list.add(new Point2D(0.0, 0.9));
        }

        return list;
    }

    private void updateImage() {
        if (pixelBuffer == null) {
            return;
        }
        int h = (int) getHeight();

        NamedColorSpace cs = getColorSpace();
        List<Point2D> point2DS = getStimuli(cs);
        Path2D.Float shape = new Path2D.Float();
        for (int i = 0; i < point2DS.size(); i++) {
            Point2D p = point2DS.get(i);
            if (i == 0) {
                shape.moveTo(p.getX(), p.getY());
            } else {
                shape.lineTo(p.getX(), p.getY());
            }
        }
        shape.closePath();

        RangeTask.forEach(0, h, 100, new UpdateRange(cs, pixelBuffer, shape, getDisplayColorSpace()));
        pixelBuffer.updateBuffer(pb -> new Rectangle2D(0, 0, pb.getWidth(), pb.getHeight()));
    }

    record UpdateRange(NamedColorSpace cs, PixelBuffer<IntBuffer> pxBuf,
                       Path2D.Float shape,
                       NamedColorSpace displayCS)
            implements BiConsumer<Integer, Integer> {

        private static final float EPSILON = 1e-3f;

        @Override
        public void accept(Integer fromInteger, Integer toInteger) {
            acceptAsInt(fromInteger, toInteger);
        }

        public void acceptAsInt(int from, int to) {
            int w = pxBuf.getWidth();
            int h = pxBuf.getHeight();
            int[] a = pxBuf.getBuffer().array();

            float[] xyz = new float[3];
            float[] rgb = new float[3];
            float[] displayRgb = new float[3];
            float[] colorvalue = new float[cs.getNumComponents()];

            Arrays.fill(a, from * w, to * w, 0);
            float xwScale = xwExtent / w;
            float ywScale = ywExtent / h;

            if (!shape.intersects(0, (h - to) * ywScale, xwExtent, (to - from) * ywScale)) {
                return;
            }

            float Y = 1f;
            for (int y = from, xy = from * w; y < to; y++, xy += w) {
                float yw = (h - y) * ywScale + ywMin;
                for (int x = 0; x < w; x++) {
                    float xw = x * xwScale + xwMin;
                    if (shape.contains(xw, yw)) {
                        convertChromacityToXYZ(xw, yw, Y, xyz);
                        cs.fromCIEXYZ(xyz, colorvalue);
                        scaleToMax(colorvalue);
                        cs.toRGB(colorvalue, rgb);
                        displayCS.fromRGB(rgb, displayRgb);
                        reduceChromaIfNegative(displayRgb, x, y);
                        int color = 0xff000000 | RgbBitConverters.rgbFloatToRgb24(displayRgb);
                        a[xy + x] = color;
                    }
                }
            }
        }

        private static void scaleToMax(float[] colorvalue) {
            float max = colorvalue[0];
            for (int i = 1; i < colorvalue.length; i++) {
                max = Math.max(max, colorvalue[i]);
            }
            for (int i = 0; i < colorvalue.length; i++) {
                colorvalue[i] = Math.clamp(colorvalue[i] / max, (float) 0, (float) 1);
            }
        }

        private static void reduceChromaIfTooBright(float[] colorvalue, int x, int y) {
            float max = Math.max(Math.max(colorvalue[0], colorvalue[1]), colorvalue[2]);
            if (max > 1 + EPSILON) {
                if (((x + y) & 1) == 0) {
                    colorvalue[0] = 0.5f;
                    colorvalue[1] = 0.5f;
                    colorvalue[2] = 0.5f;
                } else {
                    colorvalue[0] /= max;
                    colorvalue[1] /= max;
                    colorvalue[2] /= max;
                }
            }
        }

        private static void reduceChromaIfNegative(float[] colorvalue, int x, int y) {
            float min = Math.min(Math.min(colorvalue[0], colorvalue[1]), colorvalue[2]);
            float max = Math.max(Math.max(colorvalue[0], colorvalue[1]), colorvalue[2]);
            if (min < 0) {
                if (((x + y) & 1) == 0) {
                    colorvalue[0] = 0.5f;
                    colorvalue[1] = 0.5f;
                    colorvalue[2] = 0.5f;
                } else {
                    colorvalue[0] /= max;
                    colorvalue[1] /= max;
                    colorvalue[2] /= max;
                }
            }
        }

        private static void clampIfTooBright(float[] colorvalue) {
            colorvalue[0] = Math.clamp(colorvalue[0], (float) 0, (float) 1);
            colorvalue[1] = Math.clamp(colorvalue[1], (float) 0, (float) 1);
            colorvalue[2] = Math.clamp(colorvalue[2], (float) 0, (float) 1);
        }

        private static void grayIfTooBright(float[] colorvalue) {
            float max = Math.max(Math.max(colorvalue[0], colorvalue[1]), colorvalue[2]);
            if (max > 1 + EPSILON) {
                colorvalue[0] = 0.5f;
                colorvalue[1] = 0.5f;
                colorvalue[2] = 0.5f;
            }
        }
    }

    private static void convertChromacityToXYZ(float x, float y, float Y, float[] XYZ) {
        float X = x * Y / y;
        float Z = (1 - x - y) * Y / y;
        XYZ[0] = X;
        XYZ[1] = Y;
        XYZ[2] = Z;
    }

    private static void convertXYZtoChromacity(float[] XYZ, float[] xyY) {
        float X = XYZ[0];
        float Y = XYZ[1];
        float Z = XYZ[2];
        float x = X / (X + Y + Z);
        float y = Y / (X + Y + Z);
        xyY[0] = x;
        xyY[1] = y;
        xyY[2] = Y;
    }

    public NamedColorSpace getColorSpace() {
        return colorSpace.get();
    }

    public ObjectProperty<NamedColorSpace> colorSpaceProperty() {
        return colorSpace;
    }

    public void setColorSpace(NamedColorSpace colorSpace) {
        this.colorSpace.set(colorSpace);
    }

    public NamedColorSpace getDisplayColorSpace() {
        return displayColorSpace.get();
    }

    public ObjectProperty<NamedColorSpace> displayColorSpaceProperty() {
        return displayColorSpace;
    }

    public void setDisplayColorSpace(NamedColorSpace displayColorSpace) {
        this.displayColorSpace.set(displayColorSpace);
    }
}
