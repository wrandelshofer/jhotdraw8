package org.jhotdraw8.fxcontrols.colorchooser;

/**
 * Sample Skeleton for 'HueSaturationPane.fxml' Controller Class
 */

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.image.PixelBuffer;
import javafx.scene.input.MouseEvent;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.concurrent.TileTask;
import org.jhotdraw8.base.util.MathUtil;
import org.jhotdraw8.color.AbstractNamedColorSpace;
import org.jhotdraw8.color.RgbBitConverters;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.ToIntFunction;


/**
 * This slider shows one component dimension of an {@link AbstractNamedColorSpace}
 * in a rectangular shape.
 */
public class ColorSlider extends AbstractColorSlider {
    /**
     * The index of the color space component that is displayed along the extent of the rectangle.
     */
    private final @NonNull IntegerProperty componentIndex = new SimpleIntegerProperty(this, "componentIndex", 0);


    private final @NonNull ObjectProperty<Orientation> orientation = new SimpleObjectProperty<>(this, "orientation", Orientation.HORIZONTAL);

    @Override
    void initialize() {
        super.initialize();
        setC1(getColorSpace().getMaxValue(1));
        setC2(getColorSpace().getMaxValue(2));
        c0Property().addListener(o -> this.onComponentValueChanged(0));
        c1Property().addListener(o -> this.onComponentValueChanged(1));
        c2Property().addListener(o -> this.onComponentValueChanged(2));
        c3Property().addListener(o -> this.onComponentValueChanged(3));
    }

    private void onComponentValueChanged(int i) {
        if (i != getComponentIndex()) {
            invalidateImage();
        }
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        double width = getWidth();
        double height = getHeight();
        AbstractNamedColorSpace cs = getColorSpace();
        float vmax = cs.getMaxValue(componentIndex.get());
        float vmin = cs.getMinValue(componentIndex.get());
        switch (getOrientation()) {

            case HORIZONTAL -> {
                sliderThumb.setTranslateX((getComponent() - vmin) * width / (vmax - vmin) - sliderThumb.getWidth() * 0.5);
                sliderThumb.setTranslateY((height - sliderThumb.getHeight()) * 0.5);
            }
            case VERTICAL -> {
                sliderThumb.setTranslateY(height - (getComponent() - vmin) * height / (vmax - vmin) - sliderThumb.getHeight() * 0.5);
                sliderThumb.setTranslateX((width - sliderThumb.getWidth()) * 0.5);
            }
        }
    }

    @Override
    protected AbstractFillTask createFillTask(@NonNull PixelBuffer<IntBuffer> pixelBuffer) {
        return new FillTask(new FillTaskRecord(Objects.requireNonNull(pixelBuffer),
                getColorSpace(), getC0(), getC1(), getC2(), getC3(), getComponentIndex(), -1, getRgbFilter()), getOrientation());
    }


    protected void onMousePressedOrDragged(MouseEvent mouseEvent) {
        float width = (float) getWidth();
        float height = (float) getHeight();
        float x = MathUtil.clamp((float) mouseEvent.getX(), 0, width);
        float y = MathUtil.clamp((float) mouseEvent.getY(), 0, height);
        AbstractNamedColorSpace cs = getColorSpace();

        float vmax = cs.getMaxValue(componentIndex.get());
        float vmin = cs.getMinValue(componentIndex.get());
        switch (getOrientation()) {
            case HORIZONTAL -> {
                setComponent(x * (vmax - vmin) / width + vmin);
            }
            case VERTICAL -> {
                setComponent((height - y) * (vmax - vmin) / height + vmin);
            }
        }
    }


    private void setComponent(float v) {
        switch (getComponentIndex()) {
            case 0 -> setC0(v);
            case 1 -> setC1(v);
            case 2 -> setC2(v);
            default -> setC3(v);
        }
    }

    private float getComponent() {
        return switch (getComponentIndex()) {
            case 0 -> getC0();
            case 1 -> getC1();
            case 2 -> getC2();
            default -> getC3();
        };
    }


    static class FillTask extends AbstractFillTask {
        private final @NonNull Orientation orientation;

        public FillTask(@NonNull FillTaskRecord record, @NonNull Orientation orientation) {
            super(record);
            this.orientation = orientation;
        }

        public void accept(@NonNull TileTask.Tile tile) {
            if (orientation == Orientation.HORIZONTAL) {
                fillFineHorizontal(tile);
            } else {
                fillFineVertical(tile);
            }
        }


        public void fillFineHorizontal(@NonNull TileTask.Tile tile) {
            PixelBuffer<IntBuffer> pixelBuffer = record.pixelBuffer();
            int width = pixelBuffer.getWidth();
            int height = pixelBuffer.getHeight();
            IntBuffer b = pixelBuffer.getBuffer();
            AbstractNamedColorSpace cs = record.colorSpace();
            int xIndex = record.xIndex();
            float xmin = cs.getMinValue(xIndex);
            float xmax = cs.getMaxValue(xIndex);
            int yIndex = record.yIndex();
            float invWidth = (xmax - xmin) / (width);
            float[] colorValue = new float[Math.max(4, cs.getNumComponents())];
            colorValue[0] = record.c0();
            colorValue[1] = record.c1();
            colorValue[2] = record.c2();
            colorValue[3] = record.c3();
            float[] rgbValue = new float[3];
            int[] array = b.array();
            ToIntFunction<Integer> filter = record.rgbFilter();

            int yfrom = tile.yfrom();
            int yto = tile.yto();
            int xfrom = tile.xfrom();
            int xto = tile.xto();

            int xy = yfrom * width;
            for (int x = xfrom; x < xto; x++) {
                float xval = x * invWidth + xmin;
                colorValue[xIndex] = xval;
                int argb = RgbBitConverters.rgbFloatToArgb32(cs.toRGB(colorValue, rgbValue));
                argb = filter.applyAsInt(argb);
                array[x + xy] = argb;
            }
            for (int y = yfrom + 1; y < yto; y++) {
                System.arraycopy(array, xfrom + xy, array, xfrom + xy + y * width, xto - xfrom);
            }
        }

        public void fillFineVertical(@NonNull TileTask.Tile tile) {
            PixelBuffer<IntBuffer> pixelBuffer = record.pixelBuffer();
            int width = pixelBuffer.getWidth();
            int height = pixelBuffer.getHeight();
            IntBuffer b = pixelBuffer.getBuffer();
            AbstractNamedColorSpace cs = record.colorSpace();
            int xIndex = record.xIndex();
            float xmin = cs.getMinValue(xIndex);
            float xmax = cs.getMaxValue(xIndex);
            int yIndex = record.yIndex();
            float invHeight = (xmax - xmin) / (height);
            float[] colorValue = new float[Math.max(4, cs.getNumComponents())];
            colorValue[0] = record.c0();
            colorValue[1] = record.c1();
            colorValue[2] = record.c2();
            colorValue[3] = record.c3();
            float[] rgbValue = new float[3];
            int[] array = b.array();
            ToIntFunction<Integer> filter = record.rgbFilter();

            int yfrom = tile.yfrom();
            int yto = tile.yto();
            int xfrom = tile.xfrom();
            int xto = tile.xto();

            // Fill every single pixel
            for (int y = yfrom, xy = yfrom * width; y < yto; y++, xy += width) {
                float xval = (height - y) * invHeight + xmin;
                colorValue[xIndex] = xval;
                int argb = RgbBitConverters.rgbFloatToArgb32(cs.toRGB(colorValue, rgbValue));
                argb = filter.applyAsInt(argb);
                Arrays.fill(array, xy + xfrom, xy + xto, argb);
            }
        }
    }

    public int getComponentIndex() {
        return componentIndex.get();
    }

    public @NonNull IntegerProperty componentIndexProperty() {
        return componentIndex;
    }

    public void setComponentIndex(int xComponentIndex) {
        this.componentIndex.set(xComponentIndex);
    }

    public @NonNull Orientation getOrientation() {
        return orientation.get();
    }

    public @NonNull ObjectProperty<Orientation> orientationProperty() {
        return orientation;
    }

    public void setOrientation(@NonNull Orientation orientation) {
        this.orientation.set(orientation);
    }
}

