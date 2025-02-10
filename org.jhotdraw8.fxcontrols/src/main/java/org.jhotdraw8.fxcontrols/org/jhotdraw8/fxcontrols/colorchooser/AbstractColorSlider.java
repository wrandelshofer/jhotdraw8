/*
 * @(#)AbstractColorSlider.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxcontrols.colorchooser;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.Path;
import org.jhotdraw8.base.concurrent.TileTask;
import org.jhotdraw8.color.NamedColorSpace;
import org.jhotdraw8.color.RgbBitConverters;
import org.jhotdraw8.geom.FXPathElementsBuilder;
import org.jspecify.annotations.Nullable;

import java.awt.color.ColorSpace;
import java.io.IOException;
import java.net.URL;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for color sliders that support color spaces with up to 4 color components.
 */
public abstract class AbstractColorSlider extends Pane {
    public static final int BLOCK_SIZE_COARSE = 32;
    public static final int BLOCK_SIZE_FINE = 2;
    protected final DoubleProperty thumbTranslateX = new SimpleDoubleProperty(this, "thumbTranslateX", 0);
    protected final DoubleProperty thumbTranslateY = new SimpleDoubleProperty(this, "thumbTranslateY", -1);
    /**
     * Set this value to true when the user is adjusting a value in another control.
     * <p>
     * For example, when the user is pressing the mouse button in a slider in another control.
     */
    protected final BooleanProperty adjusting = new SimpleBooleanProperty(this, "adjusting");
    /**
     * The value of the color component with index 0.
     */
    protected final FloatProperty c0 = new SimpleFloatProperty(this, "c0");
    /**
     * The value of the color component with index 1.
     */
    protected final FloatProperty c1 = new SimpleFloatProperty(this, "c1");
    /**
     * The value of the color component with index 2.
     */
    protected final FloatProperty c2 = new SimpleFloatProperty(this, "c2");
    /**
     * The value of the color component with index 3.
     */
    protected final FloatProperty c3 = new SimpleFloatProperty(this, "c3");
    protected final FloatProperty alpha = new SimpleFloatProperty(this, "alpha");
    /**
     * The color space of the components.
     */
    protected final ObjectProperty<NamedColorSpace> targetColorSpace = new SimpleObjectProperty<>(this, "targetColorSpace");
    protected final ObjectProperty<NamedColorSpace> sourceColorSpace = new SimpleObjectProperty<>(this, "sourceColorSpace");
    /**
     * The color space of the display.
     */
    protected final ObjectProperty<NamedColorSpace> displayColorSpace = new SimpleObjectProperty<>(this, "displayColorSpace");
    protected final ObjectProperty<ToIntFunction<Integer>> rgbFilter = new SimpleObjectProperty<>(this, "rgbFilter",
            i -> i
    );
    /**
     * Indicates whether the value of the slider should always be aligned with the tick marks.
     */
    protected final BooleanProperty snapToTicks = new SimpleBooleanProperty(this, "snapToTicks", true);
    @FXML // fx:id="sliderThumb"
    protected Region thumb; // Value injected by FXMLLoader
    @Nullable
    private PixelBuffer<IntBuffer> pixelBuffer;
    private @Nullable CompletableFuture<Void> fillFuture;
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;
    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;
    @FXML // fx:id="root"
    private AnchorPane root; // Value injected by FXMLLoader
    @FXML // fx:id="colorRect"
    private ImageView colorRect; // Value injected by FXMLLoader
    private boolean invalid;

    public AbstractColorSlider() {

    }

    public static URL getFxml() {
        String name = "AbstractColorSlider.fxml";
        return Objects.requireNonNull(AbstractColorSlider.class.getResource(name), name);
    }

    public BooleanProperty adjustingProperty() {
        return adjusting;
    }

    public FloatProperty c0Property() {
        return c0;
    }

    public FloatProperty c1Property() {
        return c1;
    }

    public FloatProperty c2Property() {
        return c2;
    }

    public FloatProperty c3Property() {
        return c3;
    }

    public ObjectProperty<NamedColorSpace> targetColorSpaceProperty() {
        return targetColorSpace;
    }

    protected abstract @Nullable AbstractFillTask createFillTask(PixelBuffer<IntBuffer> pixelBuffer);

    protected void drawColorRect() {
        int width = Math.max(1, (int) getWidth());
        int height = Math.max(1, (int) getHeight());
        boolean resize = pixelBuffer == null
                || pixelBuffer.getWidth() != width
                || pixelBuffer.getHeight() != height;
        Image newImage;
        PixelBuffer<IntBuffer> newPixelBuffer;
        if (resize) {
            IntBuffer intBuffer = IntBuffer.allocate(width * height);
            PixelFormat<IntBuffer> pixelFormat = PixelFormat.getIntArgbPreInstance();
            newPixelBuffer = new PixelBuffer<>(width, height, intBuffer, pixelFormat);
            newImage = new WritableImage(newPixelBuffer);

            // We do not set the new image immediately, because we have not drawn on it yet.
            // We stretch the existing image instead.
            // This gives immediate user feedback when the window is being resized.
            colorRect.setFitWidth(width);
            colorRect.setFitHeight(height);
            invalid = true;

            // Cancel the fill task, because it draws into an old pixel buffer.
            if (fillFuture != null) {
                fillFuture.cancel(false);
            }
        } else {
            newImage = colorRect.getImage();
            newPixelBuffer = pixelBuffer;
        }

        if (fillFuture != null) {
            return;
        }

        if (invalid) {
            invalid = false;
            AbstractFillTask newFillTask = createFillTask(newPixelBuffer);
            if (newFillTask == null) {
                return;
            }
            fillFuture = TileTask.fork(0, 0, width, height, 64, newFillTask);
            fillFuture.handle((v, e) -> {
                Platform.runLater(() -> {
                    // Only update image, if we have not been cancelled
                    if (e == null) {
                        if (colorRect.getImage() != newImage) {
                            colorRect.setImage(newImage);
                            colorRect.setViewport(null);//new Rectangle2D(0, 0, newImage.getWidth()+1, newImage.getHeight()+1));
                            colorRect.setFitWidth(-1);
                            colorRect.setFitHeight(-1);
                        }
                        //noinspection ReturnOfNull
                        newPixelBuffer.updateBuffer(b -> null);
                        pixelBuffer = newPixelBuffer;
                    } else if (!(e instanceof CancellationException)) {
                        Logger.getLogger(getClass().getName()).log(Level.WARNING, "Unexpected Exception " + e.getMessage(), e);

                    }

                    // If the image became invalid while we were drawing it,
                    // we have to go at it again.
                    if (invalid) {
                        requestLayout();
                    }

                    // Fill future is done
                    fillFuture = null;
                });
                //noinspection ReturnOfNull
                return null;
            });
        }
    }

    public float getC0() {
        return c0.get();
    }

    public void setC0(float c0) {
        this.c0.set(c0);
    }

    public float getC1() {
        return c1.get();
    }

    public void setC1(float c1) {
        this.c1.set(c1);
    }

    public float getC2() {
        return c2.get();
    }

    public void setC2(float c2) {
        this.c2.set(c2);
    }

    public float getC3() {
        return c3.get();
    }

    public void setC3(float c3) {
        this.c3.set(c3);
    }

    public NamedColorSpace getTargetColorSpace() {
        return targetColorSpace.get();
    }

    public void setTargetColorSpace(NamedColorSpace targetColorSpace) {
        this.targetColorSpace.set(targetColorSpace);
    }

    public ToIntFunction<Integer> getRgbFilter() {
        return rgbFilter.get();
    }

    public void setRgbFilter(ToIntFunction<Integer> rgbFilter) {
        this.rgbFilter.set(rgbFilter);
    }

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert root != null : "fx:id=\"root\" was not injected: check your FXML file 'AbstractColorSlider.fxml'.";
        assert colorRect != null : "fx:id=\"sliderArea\" was not injected: check your FXML file 'AbstractColorSlider.fxml'.";
        assert thumb != null : "fx:id=\"sliderThumb\" was not injected: check your FXML file 'AbstractColorSlider.fxml'.";

        colorRect.setPreserveRatio(false);
        colorRect.setSmooth(false);


        Path path = new Path();
        var b = new FXPathElementsBuilder(path.getElements());
        b.circle(9, 0, 0);
        b.circle(12, 0, 0);
        path.setFillRule(FillRule.EVEN_ODD);
        thumb.setShape(path);
        thumb.setBackground(new Background(new BackgroundFill(Color.rgb(250, 250, 250),
                null, null)));
        thumb.setEffect(new DropShadow(BlurType.THREE_PASS_BOX, Color.BLACK, 2, 0.0, 0, 1));

        // XXX this should be done by a stylesheet
        focusedProperty().addListener((o, oldv, newv) -> {
            if (newv) {
                thumb.setBorder(new Border(
                        new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, null, null, new Insets(-1, -1, -1, -1))//,
                        // new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID,null,null,new Insets(2,2,2,2))
                ));
            } else {
                thumb.setBorder(null);
            }
        });

        setOnMousePressed(this::onMousePressedOrDragged);
        setOnMouseDragged(this::onMousePressedOrDragged);
        setOnKeyPressed(this::onKeyPressed);
        // setOnMouseReleased(this::onMouseReleased);
        adjustingProperty().addListener(this::onAdjusting);

        InvalidationListener propertyListener = o -> requestLayout();
        rgbFilterProperty().addListener(propertyListener);
        targetColorSpaceProperty().addListener(propertyListener);
        sourceColorSpaceProperty().addListener(propertyListener);
        displayColorSpaceProperty().addListener(propertyListener);
        alphaProperty().addListener(propertyListener);
        alphaProperty().addListener(propertyListener);


        thumb.setId("color-rect-indicator");
        getStyleClass().add("color-rect-pane");

    }

    protected abstract void onKeyPressed(KeyEvent keyEvent);

    public void invalidate() {
        if (!invalid) {
            invalid = true;
            requestLayout();
        }
    }

    public boolean isAdjusting() {
        return adjusting.get();
    }

    public void setAdjusting(boolean adjusting) {
        this.adjusting.set(adjusting);
    }

    public boolean isSnapToTicks() {
        return snapToTicks.get();
    }

    public void setSnapToTicks(boolean snapToTicks) {
        this.snapToTicks.set(snapToTicks);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        int width = Math.max(1, (int) getWidth());
        int height = Math.max(1, (int) getHeight());
        invalid |= pixelBuffer == null
                || pixelBuffer.getWidth() != width
                || pixelBuffer.getHeight() != height;

        validateColorRect();
    }

    /**
     * Must be called from the constructor of the subclass!
     */
    protected void load() {
        try {
            FXMLLoader loader = new FXMLLoader(AbstractColorSlider.getFxml());
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
        } catch (IOException exc) {
            throw new RuntimeException(exc);
        }
    }

    protected float maybeSnapToTicks(float value, double tickUnit, MouseEvent mouseEvent) {
        return !mouseEvent.isAltDown() && isSnapToTicks()
                ? (float) (Math.round(value / tickUnit) * tickUnit)
                : value;
    }

    private void onAdjusting(Observable observable) {
        // if (!isPressed() && !isAdjusting()) {
        invalidate();
        // }
    }

    protected abstract void onMousePressedOrDragged(MouseEvent mouseEvent);

    public ObjectProperty<ToIntFunction<Integer>> rgbFilterProperty() {
        return rgbFilter;
    }

    public BooleanProperty snapToTicksProperty() {
        return snapToTicks;
    }

    private void validateColorRect() {
        if (invalid) {
            drawColorRect();
        }
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

    public record FillTaskRecord(PixelBuffer<IntBuffer> pixelBuffer,
                                 NamedColorSpace sourceColorSpace, NamedColorSpace targetColorSpace,
                                 NamedColorSpace displayColorSpace,
                          float c0, float c1, float c2, float c3,
                          int xIndex, int yIndex,
                                 float alpha, ToIntFunction<Integer> rgbFilter
    ) {
    }

    public abstract static class AbstractFillTask implements Consumer<TileTask.Tile> {
        protected final FillTaskRecord record;

        public AbstractFillTask(FillTaskRecord record) {
            this.record = record;
        }
    }

    public double getThumbTranslateX() {
        return thumbTranslateX.get();
    }

    public DoubleProperty thumbTranslateXProperty() {
        return thumbTranslateX;
    }

    /**
     * If epsilon is 0, we get the best rendering with OK LCH: the shape is a triangle with a straight line
     * from the bottom left to the right, and a curved line from the right to the top left. (as shown below).
     * <p>
     * However, with epsilon 0, pure white color converted from sRGB to Display P3 is out of gamut, because
     * the converted color value is slightly greater than 1.0.
     * <pre>
     *     +---
     *     |   ----
     *     |       ------
     *     |             --------
     *     |             ----
     *     |         ----
     *     |    ----
     *     +----
     * </pre>
     * <p>
     * If epsilon is too large, we get wiggles in the OK LCH rendering: the shape wiggles at the bottom left
     * before it goes to the right. The curved line from the right to the top left is still good.
     * <pre>
     *     +---
     *     |   ----
     *     |       ------
     *     |             --------
     *     |             ----
     *     |         ----
     *     |      ----
     *     +----------
     * </pre>
     *
     * @param colorSpace a color space
     * @param component component values in the specified color space
     * @return true if at least one component value is out of gamut
     */
    protected static boolean outOfGamut(NamedColorSpace colorSpace, float[] component) {
        float epsMax;
        float epsMin;
        if (colorSpace.getType() == ColorSpace.TYPE_RGB) {
            epsMax = 0x1p-10f;
            epsMin = 0f;
        } else {
            epsMax = 0f;
            epsMin = 0f;
        }
        return component[0] < colorSpace.getMinValue(0) - epsMin
                || component[0] > colorSpace.getMaxValue(0) + epsMax
                || component[1] < colorSpace.getMinValue(1) - epsMin
                || component[1] > colorSpace.getMaxValue(1) + epsMax
                || component[2] < colorSpace.getMinValue(2) - epsMin
                || component[2] > colorSpace.getMaxValue(2) + epsMax;
    }

    protected static int getPreArgb(NamedColorSpace dcs, float[] dRgb, float[] pre, float alpha) {
        int argb = RgbBitConverters.rgbFloatToPreArgb32(dRgb, alpha, pre);
        return argb;
    }

    protected static int getArgb(NamedColorSpace scs, NamedColorSpace tcs, NamedColorSpace dcs, float[] colorValue, float[] sRgb, float[] tComponent, float[] dComponent, float[] dRgb, float alpha) {
        scs.toRGB(colorValue, sRgb);
        if (tcs == scs) {
            System.arraycopy(colorValue, 0, tComponent, 0, 3);
        } else {
            tcs.fromRGB(sRgb, tComponent);
        }
        if (dcs == scs) {
            System.arraycopy(colorValue, 0, dComponent, 0, 3);
        } else {
            dcs.fromRGB(sRgb, dComponent);
        }

        boolean outOfDisplay = outOfGamut(dcs, dComponent);
        boolean outOfTarget = outOfGamut(tcs, tComponent);
        if (outOfDisplay) {
            //   System.out.println(Arrays.toString(dComponent));
        }
        if (!outOfTarget && outOfDisplay) {
            dRgb[0] = .5f;
            dRgb[1] = .5f;
            dRgb[2] = .5f;
        } else {
            if (dcs.getType() == ColorSpace.TYPE_RGB) {
                System.arraycopy(dComponent, 0, dRgb, 0, 3);
            } else {
                dcs.toRGB(dComponent, dRgb);
            }
        }
        int argb = RgbBitConverters.rgbFloatToPreArgb32(dRgb, outOfTarget ? 0 : alpha, dRgb);
        return argb;
    }

    public void setThumbTranslateX(double thumbTranslateX) {
        this.thumbTranslateX.set(thumbTranslateX);
    }

    public double getThumbTranslateY() {
        return thumbTranslateY.get();
    }

    public DoubleProperty thumbTranslateYProperty() {
        return thumbTranslateY;
    }

    public void setThumbTranslateY(double thumbTranslateY) {
        this.thumbTranslateY.set(thumbTranslateY);
    }

    public NamedColorSpace getSourceColorSpace() {
        return sourceColorSpace.get();
    }

    public ObjectProperty<NamedColorSpace> sourceColorSpaceProperty() {
        return sourceColorSpace;
    }

    public void setSourceColorSpace(NamedColorSpace sourceColorSpace) {
        this.sourceColorSpace.set(sourceColorSpace);
    }

    public float getAlpha() {
        return alpha.get();
    }

    public FloatProperty alphaProperty() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha.set(alpha);
    }
}
