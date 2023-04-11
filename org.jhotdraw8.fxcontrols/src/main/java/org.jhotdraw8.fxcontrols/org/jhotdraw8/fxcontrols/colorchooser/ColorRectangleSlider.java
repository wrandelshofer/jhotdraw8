package org.jhotdraw8.fxcontrols.colorchooser;

/**
 * Sample Skeleton for 'HueSaturationPane.fxml' Controller Class
 */

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.image.PixelBuffer;
import javafx.scene.input.MouseEvent;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.util.MathUtil;
import org.jhotdraw8.color.AbstractNamedColorSpace;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.ToIntFunction;


/**
 * This slider shows two component dimension of an {@link AbstractNamedColorSpace}
 * in a rectangular shape.
 */
public class ColorRectangleSlider extends AbstractColorSlider {
    /**
     * The index of the color space component that is displayed along the x-axis of the rectangle.
     */
    private final @NonNull IntegerProperty xComponentIndex = new SimpleIntegerProperty(this, "xComponentIndex", 1);
    /**
     * The index of the color space component that is displayed along the y-axis of the rectangle.
     */
    private final @NonNull IntegerProperty yComponentIndex = new SimpleIntegerProperty(this, "yComponentIndex", 2);

    @Override
    void initialize() {
        super.initialize();
        c0Property().addListener(o -> this.onComponentValueChanged(0));
        c1Property().addListener(o -> this.onComponentValueChanged(1));
        c2Property().addListener(o -> this.onComponentValueChanged(2));
        c3Property().addListener(o -> this.onComponentValueChanged(3));
    }

    private void onComponentValueChanged(int i) {
        if (i != getXComponentIndex() && i != getYComponentIndex()) {
            updateImage();
        }
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        double width = getWidth();
        double height = getHeight();
        AbstractNamedColorSpace cs = getColorSpace();
        float xmax = cs.getMaxValue(xComponentIndex.get());
        float xmin = cs.getMinValue(xComponentIndex.get());
        float ymax = cs.getMaxValue(yComponentIndex.get());
        float ymin = cs.getMinValue(yComponentIndex.get());
        sliderThumb.setTranslateX((getXComponent() - xmin) * width / (xmax - xmin) - sliderThumb.getWidth() * 0.5);
        sliderThumb.setTranslateY((getYComponent() - ymin) * height / (ymax - ymin) - sliderThumb.getHeight() * 0.5);
    }

    @Override
    protected @NonNull AbstractColorSlider.AbstractFillTask createFillTask() {
        return new FillTask(new FillTaskRecord(Objects.requireNonNull(pixelBuffer),
                getColorSpace(), getC0(), getC1(), getC2(), getC3(), getXComponentIndex(), getYComponentIndex(),
                getRgbFilter()));
    }


    protected void onMousePressedOrDragged(MouseEvent mouseEvent) {
        float width = (float) getWidth();
        float height = (float) getHeight();
        float x = MathUtil.clamp((float) mouseEvent.getX(), 0, width);
        float y = MathUtil.clamp((float) mouseEvent.getY(), 0, height);
        AbstractNamedColorSpace cs = getColorSpace();
        float xmax = cs.getMaxValue(xComponentIndex.get());
        float xmin = cs.getMinValue(xComponentIndex.get());
        float ymax = cs.getMaxValue(yComponentIndex.get());
        float ymin = cs.getMinValue(yComponentIndex.get());
        setXComponent(x * (xmax - xmin) / width + xmin);
        setYComponent(y * (ymax - ymin) / height + ymin);
    }

    @Override
    protected boolean needsUpdate(float[] oldValue, float[] newValue) {

        int xIndex = getXComponentIndex();
        int yIndex = getYComponentIndex();
        for (int i = 0; i < oldValue.length; i++) {
            if (i != xIndex && i != yIndex) {
                if (oldValue[i] != newValue[i]) return true;
            }
        }
        return false;
    }

    private void setYComponent(float v) {
        switch (getYComponentIndex()) {
            case 0 -> setC0(v);
            case 1 -> setC1(v);
            case 2 -> setC2(v);
            default -> setC3(v);
        }
    }

    private void setXComponent(float v) {
        switch (getXComponentIndex()) {
            case 0 -> setC0(v);
            case 1 -> setC1(v);
            case 2 -> setC2(v);
            default -> setC3(v);
        }
    }

    private float getXComponent() {
        return switch (getXComponentIndex()) {
            case 0 -> getC0();
            case 1 -> getC1();
            case 2 -> getC2();
            default -> getC3();
        };
    }

    private float getYComponent() {
        return switch (getYComponentIndex()) {
            case 0 -> getC0();
            case 1 -> getC1();
            case 2 -> getC2();
            default -> getC3();
        };
    }


    class FillTask extends AbstractFillTask {
        public FillTask(@NonNull FillTaskRecord record) {
            super(record);
        }

        @Override
        public void fill(int blockSize) {
            if (blockSize <= 1) fillFine();
            else fillBlocks(blockSize);
        }

        public void fillBlocks(int blockSize) {
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
            float ymin = cs.getMinValue(yIndex);
            float ymax = cs.getMaxValue(yIndex);
            float ybase = ymax;
            float invWidth = (xmax - xmin) / (width);
            float invHeight = -(ymax - ymin) / (height);
            float[] colorValue = new float[Math.max(4, cs.getNumComponents())];
            colorValue[0] = record.c0();
            colorValue[1] = record.c1();
            colorValue[2] = record.c2();
            colorValue[3] = record.c3();
            float[] rgbValue = new float[3];
            int[] array = b.array();
            ToIntFunction<Integer> filter = record.rgbFilter();

            // Fill blocks
            for (int y = 0, xy = 0; y < height; y += blockSize, xy += width * blockSize) {
                if (isCancelled()) {
                    return;
                }
                for (int x = 0; x < width; x += blockSize) {
                    float xval = Math.min(width, x + (blockSize >>> 1)) * invWidth + xmin;
                    float yval = Math.min(height, y + (blockSize >>> 1)) * invHeight + ybase;
                    colorValue[xIndex] = xval;
                    colorValue[yIndex] = yval;

                    int argb = cs.toRgb24(colorValue, rgbValue);
                    argb = filter.applyAsInt(argb);

                    Arrays.fill(array, x + xy, Math.min(xy + width, x + xy + blockSize), argb);
                }

                for (int i = 1; i < blockSize; i <<= 1) {
                    if (y < height - i) {
                        System.arraycopy(array, xy, array, xy + width * i, width * Math.min(i, height - y - i));
                    }
                }
            }
        }

        public void fillFine() {
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
            float ymin = cs.getMinValue(yIndex);
            float ymax = cs.getMaxValue(yIndex);
            float ybase = ymax;
            float invWidth = (xmax - xmin) / (width);
            float invHeight = -(ymax - ymin) / (height);
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
                    float yval = y * invHeight + ybase;
                    colorValue[xIndex] = xval;
                    colorValue[yIndex] = yval;

                    int argb = cs.toRgb24(colorValue, rgbValue);
                    argb = filter.applyAsInt(argb);
                    array[x + xy] = argb;
                }
            }
        }
    }

    public int getXComponentIndex() {
        return xComponentIndex.get();
    }

    public @NonNull IntegerProperty xComponentIndexProperty() {
        return xComponentIndex;
    }

    public void setXComponentIndex(int xComponentIndex) {
        this.xComponentIndex.set(xComponentIndex);
    }

    public int getYComponentIndex() {
        return yComponentIndex.get();
    }

    public @NonNull IntegerProperty yComponentIndexProperty() {
        return yComponentIndex;
    }

    public void setYComponentIndex(int yComponentIndex) {
        this.yComponentIndex.set(yComponentIndex);
    }
}

