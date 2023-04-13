package org.jhotdraw8.fxcontrols.colorchooser;

/**
 * Sample Skeleton for 'HueSaturationPane.fxml' Controller Class
 */

import javafx.beans.InvalidationListener;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.image.PixelBuffer;
import javafx.scene.input.MouseEvent;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.concurrent.TileTask;
import org.jhotdraw8.color.NamedColorSpace;
import org.jhotdraw8.color.RgbBitConverters;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.ToIntFunction;

import static org.jhotdraw8.base.util.MathUtil.clamp;


/**
 * This slider shows one component dimension of an {@link NamedColorSpace}
 * in a rectangular shape.
 */
public class AlphaSlider extends AbstractColorSlider {


    private final @NonNull ObjectProperty<Orientation> orientation = new SimpleObjectProperty<>(this, "orientation", Orientation.HORIZONTAL);
    /**
     * The value of the alpha component.
     */
    private final @NonNull FloatProperty alpha = new SimpleFloatProperty(this, "alpha", 1.0f);
    private final @NonNull FloatProperty alphaMaxValue = new SimpleFloatProperty(this, "alphaMaxValue", 1.0f);
    private final @NonNull FloatProperty alphaMinValue = new SimpleFloatProperty(this, "alphaMinValue", 0.0f);

    /**
     * The unit distance between tick marks.
     */
    private final @NonNull FloatProperty tickUnit = new SimpleFloatProperty(this, "tickUnit", 1f / 255);


    public AlphaSlider() {
        load();
    }

    @Override
    void initialize() {
        super.initialize();
        setC1(getColorSpace().getMaxValue(1));
        setC2(getColorSpace().getMaxValue(2));
        InvalidationListener handler = o -> invalidateImage();
        c0Property().addListener(handler);
        c1Property().addListener(handler);
        c2Property().addListener(handler);
        c3Property().addListener(handler);
        alphaMaxValueProperty().addListener(handler);
        alphaMinValueProperty().addListener(handler);
    }


    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        double width = getWidth();
        double height = getHeight();
        NamedColorSpace cs = getColorSpace();
        float vmax = getAlphaMaxValue();
        float vmin = getAlphaMinValue();
        switch (getOrientation()) {

            case HORIZONTAL -> {
                sliderThumb.setTranslateX((getAlpha() - vmin) * width / (vmax - vmin) - sliderThumb.getWidth() * 0.5);
                sliderThumb.setTranslateY((height - sliderThumb.getHeight()) * 0.5);
            }
            case VERTICAL -> {
                sliderThumb.setTranslateY(height - (getAlpha() - vmin) * height / (vmax - vmin) - sliderThumb.getHeight() * 0.5);
                sliderThumb.setTranslateX((width - sliderThumb.getWidth()) * 0.5);
            }
        }
    }

    @Override
    protected AbstractFillTask createFillTask(@NonNull PixelBuffer<IntBuffer> pixelBuffer) {
        return new FillTask(new FillTaskRecord(Objects.requireNonNull(pixelBuffer),
                getDisplayColorSpace(),
                getColorSpace(), getC0(), getC1(), getC2(), getC3(), 4, -1, getRgbFilter()), getOrientation(), getAlphaMinValue(), getAlphaMaxValue());
    }


    protected void onMousePressedOrDragged(MouseEvent mouseEvent) {
        float width = (float) getWidth();
        float height = (float) getHeight();
        float x = clamp((float) mouseEvent.getX(), 0, width);
        float y = clamp((float) mouseEvent.getY(), 0, height);

        float vmax = getAlphaMaxValue();
        float vmin = getAlphaMinValue();
        switch (getOrientation()) {
            case HORIZONTAL -> {
                setAlpha(maybeSnapToTicks(x * (vmax - vmin) / width + vmin, getTickUnit()));
            }
            case VERTICAL -> {
                setAlpha(maybeSnapToTicks((height - y) * (vmax - vmin) / height + vmin, getTickUnit()));
            }
        }
        requestLayout();
    }

    private final static float[] gray = {0.8f, 0.8f, 0.8f};
    private final static float[] white = {1f, 1f, 1f};

    static class FillTask extends AbstractFillTask {
        private final @NonNull Orientation orientation;
        private final float alphaMin;
        private final float alphaMax;

        public FillTask(@NonNull FillTaskRecord record, @NonNull Orientation orientation, float alphaMin, float alphaMax) {
            super(record);
            this.orientation = orientation;
            this.alphaMin = alphaMin;
            this.alphaMax = alphaMax;
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
            NamedColorSpace cs = record.colorSpace();
            NamedColorSpace dcs = record.displayColorSpace();
            float xmin = alphaMin;
            float xmax = alphaMax;
            float invWidth = (xmax - xmin) / (width);
            float[] colorValue = new float[Math.max(4, cs.getNumComponents())];
            colorValue[0] = record.c0();
            colorValue[1] = record.c1();
            colorValue[2] = record.c2();
            colorValue[3] = record.c3();
            float[] rgbValue = new float[3];
            float[] blendedRgb = new float[3];
            RgbBitConverters.rgbFloatToRgb24(dcs.fromRGB(cs.toRGB(colorValue, rgbValue), rgbValue));
            int[] array = b.array();
            ToIntFunction<Integer> filter = record.rgbFilter();

            int yfrom = tile.yfrom();
            int yto = tile.yto();
            int xfrom = tile.xfrom();
            int xto = tile.xto();

            for (int y = yfrom, xy = yfrom * width; y < yto; y += 2, xy += width * 2) {
                for (int x = xfrom; x < xto; x++) {
                    float alpha = x * invWidth + xmin;
                    float[] background = (((x >>> 1) + (y >>> 1)) & 1) == 0 ? white : gray;
                    for (int i = 0; i < blendedRgb.length; i++) {
                        blendedRgb[i] = rgbValue[i] * alpha + background[i] * (1 - alpha);
                    }
                    int argb = RgbBitConverters.rgbFloatToArgb32(blendedRgb);
                    argb = filter.applyAsInt(argb);
                    array[x + xy] = argb;
                }
                if (y < yto - 1) {
                    System.arraycopy(array, xy + xfrom, array, xy + xfrom + width, xto - xfrom);
                }
            }
        }

        public void fillFineVertical(@NonNull TileTask.Tile tile) {
            PixelBuffer<IntBuffer> pixelBuffer = record.pixelBuffer();
            int width = pixelBuffer.getWidth();
            int height = pixelBuffer.getHeight();
            IntBuffer b = pixelBuffer.getBuffer();
            NamedColorSpace cs = record.colorSpace();
            NamedColorSpace dcs = record.displayColorSpace();
            float ymin = alphaMin;
            float ymax = alphaMax;
            float invHeight = (ymax - ymin) / (height);
            float[] colorValue = new float[Math.max(4, cs.getNumComponents())];
            float[] blendedRgb = new float[3];
            colorValue[0] = record.c0();
            colorValue[1] = record.c1();
            colorValue[2] = record.c2();
            colorValue[3] = record.c3();
            float[] rgbValue = new float[3];
            RgbBitConverters.rgbFloatToRgb24(dcs.fromRGB(cs.toRGB(colorValue, rgbValue), rgbValue));
            int[] array = b.array();
            ToIntFunction<Integer> filter = record.rgbFilter();

            int yfrom = tile.yfrom();
            int yto = tile.yto();
            int xfrom = tile.xfrom();
            int xto = tile.xto();

            // Fill every single pixel
            for (int y = yfrom, xy = yfrom * width; y < yto; y++, xy += width) {
                float alpha = (height - y) * invHeight + ymin;
                float[] background = (((xfrom >>> 1) + (y >>> 1)) & 1) == 0 ? white : gray;
                for (int i = 0; i < blendedRgb.length; i++) {
                    blendedRgb[i] = rgbValue[i] * alpha + background[i] * (1 - alpha);
                }
                int argb = RgbBitConverters.rgbFloatToArgb32(blendedRgb);
                argb = filter.applyAsInt(argb);
                Arrays.fill(array, xy + xfrom, xy + xto, argb);
            }
        }

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

    public float getTickUnit() {
        return tickUnit.get();
    }

    public @NonNull FloatProperty tickUnitProperty() {
        return tickUnit;
    }

    public void setTickUnit(float tickUnit) {
        this.tickUnit.set(tickUnit);
    }

    public float getAlpha() {
        return alpha.get();
    }

    public @NonNull FloatProperty alphaProperty() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha.set(alpha);
    }

    public float getAlphaMaxValue() {
        return alphaMaxValue.get();
    }

    public @NonNull FloatProperty alphaMaxValueProperty() {
        return alphaMaxValue;
    }

    public void setAlphaMaxValue(float alphaMaxValue) {
        this.alphaMaxValue.set(alphaMaxValue);
    }

    public float getAlphaMinValue() {
        return alphaMinValue.get();
    }

    public @NonNull FloatProperty alphaMinValueProperty() {
        return alphaMinValue;
    }

    public void setAlphaMinValue(float alphaMinValue) {
        this.alphaMinValue.set(alphaMinValue);
    }
}

