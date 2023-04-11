package org.jhotdraw8.examples.colorspace;

import javafx.beans.InvalidationListener;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.linalg.Matrix3x3;

import java.awt.color.ColorSpace;


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
public class ChromacityDiagram extends Pane {
    private final @NonNull Canvas canvas = new Canvas();
    private final @NonNull Canvas xruler = new Canvas();
    private final @NonNull Canvas yruler = new Canvas();

    private final @NonNull ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);

    private final int RULER_WIDTH = 10;

    public ChromacityDiagram() {
        getChildren().addAll(canvas, xruler, yruler);
        InvalidationListener invalidationListener = e -> update();
        //layoutBoundsProperty().addListener(invalidationListener);
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        canvas.setWidth(w - RULER_WIDTH);
        canvas.setHeight(h - RULER_WIDTH);
        xruler.setWidth(w - RULER_WIDTH);
        xruler.setHeight(RULER_WIDTH);
        yruler.setWidth(RULER_WIDTH);
        yruler.setHeight(h - RULER_WIDTH);
        xruler.relocate(RULER_WIDTH, h - RULER_WIDTH);
        update();
    }

    public void update() {
        updateCanvas(canvas);
        updateXRuler(xruler);
        updateYRuler(yruler);
    }

    public void updateCanvas(Canvas c) {
        float w = (float) c.getWidth();
        float h = (float) c.getHeight();

        GraphicsContext gc = c.getGraphicsContext2D();
        PixelWriter pw = gc.getPixelWriter();

        float xwMax = 0.8f;
        float xwMin = 0f;
        float xwExtent = xwMax - xwMin;
        float ywMax = 0.9f;
        float ywMin = 0f;
        float ywExtent = ywMax - ywMin;
        float Y = 1f;

        float[] xyz = new float[3];
        for (int y = 0; y < h; y++) {
            float yw = y * ywExtent / h + ywMin;
            for (int x = 0; x < w; x++) {
                float xw = x * xwExtent / h + xwMin;
                float zw = 1 - x - y;
                    /*
                    xyz[0]=Y/yw*xw;
                    xyz[1]=Y;
                    xyz[2]=Y/yw*zw;
                    */
                xyz[0] = xw;
                xyz[1] = yw;
                xyz[2] = zw;
                float[] srgb = cs.fromCIEXYZ(xyz);
                if (0 <= srgb[0] && srgb[0] <= 1
                ) {
                    gc.setFill(new Color(srgb[0], srgb[1], srgb[2], 1));
                    gc.fillRect(x, y, 1, 1);
                }
            }
        }
    }

    public void updateXRuler(Canvas c) {
        int w = (int) c.getWidth();
        int h = (int) c.getHeight();

        GraphicsContext gc = c.getGraphicsContext2D();
        PixelWriter pw = gc.getPixelWriter();

        gc.setStroke(Color.BLACK);
        gc.moveTo(0, 1);
        gc.lineTo(w, 1);
        gc.stroke();

        gc.setFill(Color.RED);
        gc.fillRect(0, 0, w, h);
        gc.fill();
    }

    public void updateYRuler(Canvas c) {
        int w = (int) c.getWidth();
        int h = (int) c.getHeight();

        GraphicsContext gc = c.getGraphicsContext2D();
        PixelWriter pw = gc.getPixelWriter();

        gc.setStroke(Color.BLACK);
        gc.moveTo(w, h);
        gc.lineTo(w, 0);
        gc.stroke();
    }

    /**
     * References:
     * <dl>
     *     <dt>C. A. Bouman: Digital Image Processing - January 9, 2023, Chromacity Coordinates.</dt>
     *     <dd><a href="https://engineering.purdue.edu/~bouman/ece637/notes/pdf/ColorSpaces.pdf">purdue.edu</a></dd>
     * </dl>
     *
     * @param xyRed
     * @param xyGreen
     * @param xyBlue
     * @param whitePoint
     */
    public void computeLinearMatrix(Point2D xyRed, Point2D xyGreen, Point2D xyBlue,
                                    Point3D whitePoint) {
        // matrix M
        // [xr xg xb]
        // [yr yg yb]
        // [zr zg zb]
        Matrix3x3 M = new Matrix3x3(
                xyRed.getX(), xyGreen.getX(), xyBlue.getX(),
                xyRed.getY(), xyGreen.getY(), xyBlue.getY(),
                1 - xyRed.getX() - xyRed.getY(),
                1 - xyGreen.getX() - xyGreen.getY(),
                1 - xyBlue.getX() - xyBlue.getY());

        double X = whitePoint.getX();
        double Y = whitePoint.getY();
        double Z = whitePoint.getZ();

        // solve for a1,a2,a3
        // X       [a1  0  0]
        // Y = M * [0  a2  0]
        // Z       [0   0 a3]

        Point3D a1a2a3 = M.inv().mul(X, Y, Z);
        Matrix3x3 alpha = new Matrix3x3(
                a1a2a3.getX(), 0, 0,
                0, a1a2a3.getY(), 0,
                0, 0, a1a2a3.getZ());

        // M = M * alpha
        M = M.mul(alpha);


        System.out.println("a:" + M);
        System.out.println("a*XYZ:" + M.mul(X, Y, Z));
        System.out.println("inv(a)*XYZ:" + M.inv().mul(X, Y, Z));
        System.out.println("alpha:" + alpha);
        System.out.println("a*alpha:" + M.mul(alpha));
    }


    public static void main(String... args) {
        /*
        Matrix3x3 m = new Matrix3x3(1, 2, 3, 0, 1, 4, 5, 6, 0);
        System.out.println(m.det());
        System.out.println(m.inv());
        System.exit(0);
*/


        ChromacityDiagram cd = new ChromacityDiagram();
        cd.computeLinearMatrix(new Point2D(0.67, 0.33),
                new Point2D(0.21, 0.71),
                new Point2D(0.14, 0.08),
                new Point3D(1, 1, 1));
    }
}
