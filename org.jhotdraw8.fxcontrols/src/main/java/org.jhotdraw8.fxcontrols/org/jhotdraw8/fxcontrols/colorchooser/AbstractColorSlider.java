/*
 * @(#)AbstractColorSlider.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxcontrols.colorchooser;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
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
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.concurrent.TileTask;
import org.jhotdraw8.color.HsvColorSpace;
import org.jhotdraw8.color.NamedColorSpace;
import org.jhotdraw8.geom.FXPathElementsBuilder;

import java.io.IOException;
import java.net.URL;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

/**
 * Base class for color sliders that support color spaces with up to 4 color components.
 */
public abstract class AbstractColorSlider extends Pane {
    public static final int BLOCK_SIZE_COARSE = 32;
    public static final int BLOCK_SIZE_FINE = 2;
    /**
     * Set this value to true when the user is adjusting a value in another control.
     * <p>
     * For example, when the user is pressing the mouse button in a slider in another control.
     */
    private final @NonNull BooleanProperty adjusting = new SimpleBooleanProperty(this, "adjusting");
    /**
     * The value of the color component with index 0.
     */
    private final @NonNull FloatProperty c0 = new SimpleFloatProperty(this, "c0");
    /**
     * The value of the color component with index 1.
     */
    private final @NonNull FloatProperty c1 = new SimpleFloatProperty(this, "c1");
    /**
     * The value of the color component with index 2.
     */
    private final @NonNull FloatProperty c2 = new SimpleFloatProperty(this, "c2");
    /**
     * The value of the color component with index 3.
     */
    private final @NonNull FloatProperty c3 = new SimpleFloatProperty(this, "c3");
    /**
     * The color space of the components.
     */
    private final @NonNull ObjectProperty<NamedColorSpace> colorSpace = new SimpleObjectProperty(this, "colorSpace", new HsvColorSpace());
    /**
     * The color space of the display.
     */
    private final @NonNull ObjectProperty<NamedColorSpace> displayColorSpace = new SimpleObjectProperty(this, "displayColorSpace", new HsvColorSpace());
    private final @NonNull ObjectProperty<ToIntFunction<Integer>> rgbFilter = new SimpleObjectProperty<>(this, "rgbFilter",
            i -> (int) i
    );
    /**
     * Indicates whether the value of the slider should always be aligned with the tick marks.
     */
    private final @NonNull BooleanProperty snapToTicks = new SimpleBooleanProperty(this, "snapToTicks", true);
    @FXML // fx:id="sliderThumb"
    protected Region sliderThumb; // Value injected by FXMLLoader
    @Nullable
    private PixelBuffer<IntBuffer> pixelBuffer;
    private @Nullable CompletableFuture<Void> fillFuture;
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;
    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;
    @FXML // fx:id="root"
    private AnchorPane root; // Value injected by FXMLLoader
    @FXML // fx:id="sliderArea"
    private ImageView sliderArea; // Value injected by FXMLLoader
    private boolean invalid;

    public AbstractColorSlider() {

    }

    public static @NonNull URL getFxml() {
        String name = "AbstractColorSlider.fxml";
        return Objects.requireNonNull(AbstractColorSlider.class.getResource(name), name);
    }

    public @NonNull BooleanProperty adjustingProperty() {
        return adjusting;
    }

    public @NonNull FloatProperty c0Property() {
        return c0;
    }

    public @NonNull FloatProperty c1Property() {
        return c1;
    }

    public @NonNull FloatProperty c2Property() {
        return c2;
    }

    public @NonNull FloatProperty c3Property() {
        return c3;
    }

    public @NonNull ObjectProperty<NamedColorSpace> colorSpaceProperty() {
        return colorSpace;
    }

    protected abstract AbstractFillTask createFillTask(@NonNull PixelBuffer<IntBuffer> pixelBuffer);

    protected void drawImage() {
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
            sliderArea.setFitWidth(width);
            sliderArea.setFitHeight(height);
            invalid = true;

            // Cancel the fill task, because it draws into an old pixel buffer.
            if (fillFuture != null) {
                fillFuture.cancel(false);
            }
        } else {
            newImage = sliderArea.getImage();
            newPixelBuffer = pixelBuffer;
        }

        if (fillFuture != null) {
            return;
        }

        if (invalid) {
            invalid = false;
            AbstractFillTask newFillTask = createFillTask(newPixelBuffer);
            fillFuture = TileTask.fork(0, 0, width, height, 64, newFillTask);
            fillFuture.handle((v, e) -> {
                Platform.runLater(() -> {
                    // Only update image, if we have not been cancelled
                    if (e == null) {
                        if (sliderArea.getImage() != newImage) {
                            sliderArea.setImage(newImage);
                            sliderArea.setViewport(new Rectangle2D(0, 0, newImage.getWidth(), newImage.getHeight()));
                            sliderArea.setFitWidth(newImage.getWidth());
                            sliderArea.setFitHeight(newImage.getHeight());
                        }
                        newPixelBuffer.updateBuffer(b -> null);
                        pixelBuffer = newPixelBuffer;
                    } else if (!(e instanceof CancellationException)) {
                        e.printStackTrace();
                    }

                    // If the image became invalid while we were drawing it,
                    // we have to go at it again.
                    if (invalid) {
                        requestLayout();
                    }

                    // Fill future is done
                    fillFuture = null;
                });
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

    public NamedColorSpace getColorSpace() {
        return colorSpace.get();
    }

    public void setColorSpace(NamedColorSpace colorSpace) {
        this.colorSpace.set(colorSpace);
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
        assert sliderArea != null : "fx:id=\"sliderArea\" was not injected: check your FXML file 'AbstractColorSlider.fxml'.";
        assert sliderThumb != null : "fx:id=\"sliderThumb\" was not injected: check your FXML file 'AbstractColorSlider.fxml'.";

        sliderArea.setPreserveRatio(false);
        sliderArea.setSmooth(false);


        Path path = new Path();
        var b = new FXPathElementsBuilder(path.getElements());
        b.circle(5, 0, 0);
        b.circle(9, 0, 0);
        path.setFillRule(FillRule.EVEN_ODD);
        sliderThumb.setShape(path);
        sliderThumb.setBackground(new Background(new BackgroundFill(Color.rgb(250, 250, 250),
                null, null)));
        sliderThumb.setBorder(new Border(new BorderStroke(Color.rgb(10, 10, 10), BorderStrokeStyle.SOLID, null, null)));

        setOnMousePressed(this::onMousePressedOrDragged);
        setOnMouseDragged(this::onMousePressedOrDragged);
        // setOnMouseReleased(this::onMouseReleased);
        adjustingProperty().addListener(this::onAdjusting);

        InvalidationListener propertyListener = o -> invalidateImage();
        rgbFilterProperty().addListener(propertyListener);
        colorSpaceProperty().addListener(propertyListener);
        displayColorSpaceProperty().addListener(propertyListener);
        c0Property().addListener(propertyListener);
        c1Property().addListener(propertyListener);
        c2Property().addListener(propertyListener);
        c3Property().addListener(propertyListener);

    }

    public void invalidateImage() {
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

        validateImage();
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

    protected float maybeSnapToTicks(float value, double tickUnit) {
        return isSnapToTicks()
                ? (float) (Math.round(value / tickUnit) * tickUnit)
                : value;
    }

    private void onAdjusting(Observable observable) {
        // if (!isPressed() && !isAdjusting()) {
        invalidateImage();
        // }
    }

    protected abstract void onMousePressedOrDragged(MouseEvent mouseEvent);

    public @NonNull ObjectProperty<ToIntFunction<Integer>> rgbFilterProperty() {
        return rgbFilter;
    }

    public @NonNull BooleanProperty snapToTicksProperty() {
        return snapToTicks;
    }

    private void validateImage() {
        if (invalid) {
            drawImage();
        }
    }

    public NamedColorSpace getDisplayColorSpace() {
        return displayColorSpace.get();
    }

    public @NonNull ObjectProperty<NamedColorSpace> displayColorSpaceProperty() {
        return displayColorSpace;
    }

    public void setDisplayColorSpace(NamedColorSpace displayColorSpace) {
        this.displayColorSpace.set(displayColorSpace);
    }

    record FillTaskRecord(@NonNull PixelBuffer<IntBuffer> pixelBuffer,
                          NamedColorSpace displayColorSpace, @NonNull NamedColorSpace colorSpace,
                          float c0, float c1, float c2, float c3,
                          int xIndex, int yIndex,
                          @NonNull ToIntFunction<Integer> rgbFilter) {
    }

    abstract static class AbstractFillTask implements Consumer<TileTask.Tile> {
        protected final @NonNull FillTaskRecord record;

        public AbstractFillTask(@NonNull FillTaskRecord record) {
            this.record = record;
        }
    }
}
