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
import org.jhotdraw8.base.util.MathUtil;
import org.jhotdraw8.color.AbstractNamedColorSpace;
import org.jhotdraw8.color.RgbBitDepthConverters;

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
            updateImage();
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
    protected @NonNull AbstractColorSlider.AbstractFillTask createFillTask() {
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

    @Override
    protected boolean needsUpdate(float[] oldValue, float[] newValue) {

        int index = getComponentIndex();
        for (int i = 0; i < oldValue.length; i++) {
            if (i != index) {
                if (oldValue[i] != newValue[i]) return true;
            }
        }
        return false;
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

        @Override
        public void fill(int blockSize) {
            switch (orientation) {

                case HORIZONTAL -> {
                    if (blockSize <= 1) fillFineHorizontal();
                    else fillBlocksHorizontal(blockSize);
                }
                case VERTICAL -> {
                    if (blockSize <= 1) fillFineVertical();
                    else fillBlocksVertical(blockSize);
                }
            }
        }

        public void fillBlocksHorizontal(int blockSize) {
            if (isCancelled()) {
                return;
            }

            PixelBuffer<IntBuffer> pixelBuffer = record.pixelBuffer();
            int width = pixelBuffer.getWidth();
            int height = pixelBuffer.getHeight();
            IntBuffer b = pixelBuffer.getBuffer();
            AbstractNamedColorSpace cs = record.colorSpace();
            int xIndex = record.xIndex();
            float xmin = cs.getMinValue(xIndex);
            float xmax = cs.getMaxValue(xIndex);
            float invWidth = (xmax - xmin) / (width);
            float[] colorValue = new float[Math.max(4, cs.getNumComponents())];
            colorValue[0] = record.c0();
            colorValue[1] = record.c1();
            colorValue[2] = record.c2();
            colorValue[3] = record.c3();
            ToIntFunction<Integer> filter = record.rgbFilter();
            float[] rgbValue = new float[3];
            int[] array = b.array();

            // Fill blocks
            for (int y = 0, xy = 0; y < height; y += blockSize, xy += width * blockSize) {
                if (isCancelled()) {
                    return;
                }
                for (int x = 0; x < width; x += blockSize) {
                    float xval = Math.min(width, x + (blockSize >>> 1)) * invWidth + xmin;
                    colorValue[xIndex] = xval;
                    int argb = RgbBitDepthConverters.rgbFloatToArgb32(cs.toRGB(colorValue, rgbValue));//| 0xff_000000;
                    argb = filter.applyAsInt(argb);
                    Arrays.fill(array, x + xy, Math.min(xy + width, x + xy + blockSize), argb);
                    //array[x + xy] = argb;
                }

                for (int i = 1; i < blockSize; i <<= 1) {
                    if (y < height - i) {
                        System.arraycopy(array, xy, array, xy + width * i, width * Math.min(i, height - y - i));
                    }
                }
            }
        }

        public void fillFineHorizontal() {
            if (isCancelled()) {
                return;
            }

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


            // Fill every single pixel
            for (int y = 0, xy = 0; y < height; y++, xy += width) {
                if ((y & 31) == 0 && isCancelled()) {
                    return;
                }
                for (int x = 0; x < width; x++) {
                    float xval = x * invWidth + xmin;
                    colorValue[xIndex] = xval;
                    int argb = RgbBitDepthConverters.rgbFloatToArgb32(cs.toRGB(colorValue, rgbValue));
                    argb = filter.applyAsInt(argb);
                    array[x + xy] = argb;
                }
            }
        }

        public void fillBlocksVertical(int blockSize) {
            if (isCancelled()) {
                return;
            }

            PixelBuffer<IntBuffer> pixelBuffer = record.pixelBuffer();
            int width = pixelBuffer.getWidth();
            int height = pixelBuffer.getHeight();
            IntBuffer b = pixelBuffer.getBuffer();
            AbstractNamedColorSpace cs = record.colorSpace();
            int xIndex = record.xIndex();
            float xmin = cs.getMinValue(xIndex);
            float xmax = cs.getMaxValue(xIndex);
            float invHeight = (xmax - xmin) / (height);
            float[] colorValue = new float[Math.max(4, cs.getNumComponents())];
            colorValue[0] = record.c0();
            colorValue[1] = record.c1();
            colorValue[2] = record.c2();
            colorValue[3] = record.c3();
            ToIntFunction<Integer> filter = record.rgbFilter();
            float[] rgbValue = new float[3];
            int[] array = b.array();
            int arrayLength = array.length;

            // Fill blocks
            for (int y = 0, xy = 0; y < height; y += blockSize, xy += width * blockSize) {
                if (isCancelled()) {
                    return;
                }
                float xval = (height - y) * invHeight + xmin;
                colorValue[xIndex] = xval;
                int argb = RgbBitDepthConverters.rgbFloatToArgb32(cs.toRGB(colorValue, rgbValue));
                argb = filter.applyAsInt(argb);
                Arrays.fill(array, xy, Math.min(xy + width * blockSize, arrayLength), argb);
            }
        }

        public void fillFineVertical() {
            if (isCancelled()) {
                return;
            }

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


            // Fill every single pixel
            for (int y = 0, xy = 0; y < height; y++, xy += width) {
                if ((y & 31) == 0 && isCancelled()) {
                    return;
                }
                float xval = (height - y) * invHeight + xmin;
                colorValue[xIndex] = xval;
                int argb = RgbBitDepthConverters.rgbFloatToArgb32(cs.toRGB(colorValue, rgbValue));
                argb = filter.applyAsInt(argb);
                Arrays.fill(array, xy, xy + width, argb);
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

