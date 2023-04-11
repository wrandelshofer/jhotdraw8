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
import org.jhotdraw8.color.tmp.AbstractNamedColorSpace;
import org.jhotdraw8.color.tmp.HsvColorSpace;
import org.jhotdraw8.geom.FXPathElementsBuilder;

import java.io.IOException;
import java.net.URL;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.ToIntFunction;

/**
 * Base class for color sliders that support color spaces with up to 4 color components.
 */
public abstract class AbstractColorSlider extends Pane {
    public static final int BLOCK_SIZE_FINE = 2;
    public static final int BLOCK_SIZE_COARSE = 32;
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
     * The color space.
     */
    private final @NonNull ObjectProperty<AbstractNamedColorSpace> colorSpace = new SimpleObjectProperty(this, "colorSpace", HsvColorSpace.getInstance());
    /**
     * Set this value to true when the user is adjusting a value in another control.
     * <p>
     * For example, when the user is pressing the mouse button in a slider in another control.
     */
    private final @NonNull BooleanProperty adjusting = new SimpleBooleanProperty(this, "adjusting");


    private final @NonNull ObjectProperty<ToIntFunction<Integer>> rgbFilter = new SimpleObjectProperty<>(this, "rgbFilter",
            i -> (int) i
    );

    @Nullable
    protected PixelBuffer<IntBuffer> pixelBuffer;
    private @Nullable AbstractColorSlider.AbstractFillTask fillTask;

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="root"
    private AnchorPane root; // Value injected by FXMLLoader

    @FXML // fx:id="sliderArea"
    private ImageView sliderArea; // Value injected by FXMLLoader

    @FXML // fx:id="sliderThumb"
    protected Region sliderThumb; // Value injected by FXMLLoader

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert root != null : "fx:id=\"root\" was not injected: check your FXML file 'AbstractColorSlider.fxml'.";
        assert sliderArea != null : "fx:id=\"sliderArea\" was not injected: check your FXML file 'AbstractColorSlider.fxml'.";
        assert sliderThumb != null : "fx:id=\"sliderThumb\" was not injected: check your FXML file 'AbstractColorSlider.fxml'.";

        Path path = new Path();
        var b = new FXPathElementsBuilder(path.getElements());
        b.circle(5, 0, 0);
        b.circle(9, 0, 0);
        path.setFillRule(FillRule.EVEN_ODD);
        sliderThumb.setShape(path);
        sliderThumb.setBackground(new Background(new BackgroundFill(Color.WHITE,
                null, null)));
        sliderThumb.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, null)));

        layoutBoundsProperty().addListener(o -> this.recreateImage());
        setOnMousePressed(this::onMousePressedOrDragged);
        setOnMouseDragged(this::onMousePressedOrDragged);
        // setOnMouseReleased(this::onMouseReleased);
        adjustingProperty().addListener(this::onAdjusting);

        InvalidationListener propertyListener = o -> updateImage();
        rgbFilterProperty().addListener(propertyListener);
        colorSpaceProperty().addListener(propertyListener);

        InvalidationListener onComponentValueChanged = o -> this.requestLayout();
        c0Property().addListener(onComponentValueChanged);
        c1Property().addListener(onComponentValueChanged);
        c2Property().addListener(onComponentValueChanged);
        c3Property().addListener(onComponentValueChanged);

    }

    private void onAdjusting(Observable observable) {
        if (!isPressed() && !isAdjusting()) {
            updateImage();
        }
    }

    protected abstract void onMousePressedOrDragged(MouseEvent mouseEvent);

    private final @NonNull float[] oldValue = new float[4];
    private final @NonNull float[] newValue = new float[4];


    private void recreateImage() {
        int width = Math.max(1, (int) getWidth());
        int height = Math.max(1, (int) getHeight());

        fillValue(newValue);
        //boolean needsUpdate = (!drawCoarsenedImage && !(isAdjusting()||isPressed()))||needsUpdate(oldValue, newValue);
        boolean needsUpdate = needsUpdate(oldValue, newValue);
        if (pixelBuffer == null || pixelBuffer.getWidth() != width || pixelBuffer.getHeight() != height) {
            IntBuffer intBuffer = IntBuffer.allocate(width * height);
            PixelFormat<IntBuffer> pixelFormat = PixelFormat.getIntArgbPreInstance();
            pixelBuffer = new PixelBuffer<>(width, height, intBuffer, pixelFormat);
            Image img = new WritableImage(pixelBuffer);
            sliderArea.setFitWidth(width);
            sliderArea.setFitHeight(height);
            sliderArea.setViewport(new Rectangle2D(0, 0, width, height));
            sliderArea.setImage(img);
            needsUpdate = true;
        }
        if (needsUpdate) {
            updateImage();
        }
    }

    private void fillValue(float[] v) {
        v[0] = getC0();
        v[1] = getC1();
        v[BLOCK_SIZE_FINE] = getC2();
        v[3] = getC3();
    }

    protected abstract boolean needsUpdate(float[] oldValue, float[] newValue);

    public AbstractColorSlider() {
        try {
            FXMLLoader loader = new FXMLLoader(AbstractColorSlider.getFxml());
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
        } catch (IOException exc) {
            throw new RuntimeException(exc);
        }
    }

    public static @NonNull URL getFxml() {
        String name = "AbstractColorSlider.fxml";
        return Objects.requireNonNull(AbstractColorSlider.class.getResource(name), name);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        recreateImage();
    }

    protected void updateImage() {
        if (fillTask != null) {
            fillTask.cancel();
        }
        fillValue(oldValue);
        boolean drawFine = !isAdjusting();

        if (pixelBuffer != null) {
            // If the image is large, we fill it asynchronously
            AbstractNamedColorSpace cs = colorSpace.get();
            fillTask = createFillTask();
            if (pixelBuffer.getWidth() * pixelBuffer.getHeight() <= 640000) {
                fillTask.fill(BLOCK_SIZE_FINE);
                pixelBuffer.updateBuffer(_ignore -> null);
            } else {
                fillTask.fill(BLOCK_SIZE_COARSE);
                pixelBuffer.updateBuffer(_ignore -> null);
                if (drawFine) {
                    ForkJoinPool.commonPool().execute(fillTask);
                }
            }
        }
    }


    protected abstract @NonNull AbstractColorSlider.AbstractFillTask createFillTask();

    record FillTaskRecord(@NonNull PixelBuffer<IntBuffer> pixelBuffer,
                          @NonNull AbstractNamedColorSpace colorSpace,
                          float c0, float c1, float c2, float c3,
                          int xIndex, int yIndex,
                          @NonNull ToIntFunction<Integer> rgbFilter) {
    }

    abstract static class AbstractFillTask implements Runnable {
        private final @NonNull AtomicBoolean cancelled = new AtomicBoolean();
        protected final @NonNull FillTaskRecord record;

        public AbstractFillTask(@NonNull FillTaskRecord record) {
            this.record = record;
        }

        public void cancel() {
            cancelled.set(true);
        }

        public void fill(int blockSize) {
        }

        protected boolean isCancelled() {
            return cancelled.get();
        }

        @Override
        public void run() {
            fill(BLOCK_SIZE_FINE);
            Platform.runLater(() -> {
                if (!isCancelled()) {
                    record.pixelBuffer().updateBuffer(_ignore -> null);
                }
            });
        }
    }

    public float getC0() {
        return c0.get();
    }

    public @NonNull FloatProperty c0Property() {
        return c0;
    }

    public void setC0(float c0) {
        this.c0.set(c0);
    }

    public float getC1() {
        return c1.get();
    }

    public @NonNull FloatProperty c1Property() {
        return c1;
    }

    public void setC1(float c1) {
        this.c1.set(c1);
    }

    public float getC2() {
        return c2.get();
    }

    public @NonNull FloatProperty c2Property() {
        return c2;
    }

    public void setC2(float c2) {
        this.c2.set(c2);
    }

    public float getC3() {
        return c3.get();
    }

    public @NonNull FloatProperty c3Property() {
        return c3;
    }

    public void setC3(float c3) {
        this.c3.set(c3);
    }

    public AbstractNamedColorSpace getColorSpace() {
        return colorSpace.get();
    }

    public @NonNull ObjectProperty<AbstractNamedColorSpace> colorSpaceProperty() {
        return colorSpace;
    }

    public void setColorSpace(AbstractNamedColorSpace colorSpace) {
        this.colorSpace.set(colorSpace);
    }

    public boolean isAdjusting() {
        return adjusting.get();
    }

    public @NonNull BooleanProperty adjustingProperty() {
        return adjusting;
    }

    public void setAdjusting(boolean adjusting) {
        this.adjusting.set(adjusting);
    }

    public ToIntFunction<Integer> getRgbFilter() {
        return rgbFilter.get();
    }

    public @NonNull ObjectProperty<ToIntFunction<Integer>> rgbFilterProperty() {
        return rgbFilter;
    }

    public void setRgbFilter(ToIntFunction<Integer> rgbFilter) {
        this.rgbFilter.set(rgbFilter);
    }
}
