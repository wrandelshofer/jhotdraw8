package org.jhotdraw8.fxcontrols.colorchooser;

/**
 * Sample Skeleton for 'HueSaturationPane.fxml' Controller Class
 */

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.image.PixelBuffer;
import javafx.scene.input.MouseEvent;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.concurrent.TileTask;
import org.jhotdraw8.base.util.MathUtil;
import org.jhotdraw8.color.AbstractNamedColorSpace;
import org.jhotdraw8.color.RgbBitConverters;

import java.nio.IntBuffer;
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
    private IntegerProperty xComponentIndex = new SimpleIntegerProperty(this, "xComponentIndex", 1);
    /**
     * The index of the color space component that is displayed along the y-axis of the rectangle.
     */
    private IntegerProperty yComponentIndex = new SimpleIntegerProperty(this, "yComponentIndex", 2);

    @Override
    void initialize() {
        super.initialize();
        c0Property().addListener(o -> this.onComponentValueChanged(0));
        c1Property().addListener(o -> this.onComponentValueChanged(1));
        c2Property().addListener(o -> this.onComponentValueChanged(2));
        c3Property().addListener(o -> this.onComponentValueChanged(3));

        xComponentIndex = new SimpleIntegerProperty(this, "xComponentIndex", 1);
        yComponentIndex = new SimpleIntegerProperty(this, "yComponentIndex", 2);

        xComponentIndex.addListener(o -> invalidateImage());
        yComponentIndex.addListener(o -> invalidateImage());
    }

    private void onComponentValueChanged(int i) {
        if (i != getXComponentIndex() && i != getYComponentIndex()) {
            invalidateImage();
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
        sliderThumb.setTranslateY(height - (getYComponent() - ymin) * height / (ymax - ymin) - sliderThumb.getHeight() * 0.5);
    }

    @Override
    protected AbstractFillTask createFillTask(@NonNull PixelBuffer<IntBuffer> pixelBuffer) {
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
        setYComponent((height - y) * (ymax - ymin) / height + ymin);
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

        public void accept(TileTask.Tile tile) {
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

            int yfrom = tile.yfrom();
            int yto = tile.yto();
            int xfrom = tile.xfrom();
            int xto = tile.xto();


            // Fill every single pixel
            for (int y = yfrom, xy = yfrom * width; y < yto; y++, xy += width) {
                for (int x = xfrom; x < xto; x++) {
                    float xval = x * invWidth + xmin;
                    float yval = y * invHeight + ybase;
                    colorValue[xIndex] = xval;
                    colorValue[yIndex] = yval;

                    cs.toRGB(colorValue, rgbValue);
                    int argb = RgbBitConverters.rgbFloatToArgb32(rgbValue);
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

