package org.jhotdraw8.fxcontrols.colorchooser;

/**
 * Sample Skeleton for 'HueSaturationPane.fxml' Controller Class
 */

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.image.PixelBuffer;
import javafx.scene.input.MouseEvent;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.concurrent.TileTask;
import org.jhotdraw8.base.util.MathUtil;
import org.jhotdraw8.color.NamedColorSpace;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.ToIntFunction;


/**
 * This slider shows one component dimension of an {@link NamedColorSpace}
 * in a rectangular shape.
 */
public class ColorSlider extends AbstractColorSlider {
    /**
     * The index of the color space component that is displayed along the extent of the rectangle.
     * <p>
     * Alpha has index 4.
     */
    private final @NonNull IntegerProperty componentIndex = new SimpleIntegerProperty(this, "componentIndex", 0);


    private final @NonNull ObjectProperty<Orientation> orientation = new SimpleObjectProperty<>(this, "orientation", Orientation.HORIZONTAL);

    /**
     * The unit distance between tick marks.
     * <p>
     * This must be a double property (and not float) so that we do not run into rounding issues.
     */
    private final @NonNull DoubleProperty tickUnit = new SimpleDoubleProperty(this, "tickUnit", 1d / 255);

    public ColorSlider() {
        load();
    }

    @Override
    void initialize() {
        super.initialize();
        c0Property().addListener(o -> this.onComponentValueChanged(0));
        c1Property().addListener(o -> this.onComponentValueChanged(1));
        c2Property().addListener(o -> this.onComponentValueChanged(2));
        c3Property().addListener(o -> this.onComponentValueChanged(3));
    }

    private void onComponentValueChanged(int i) {
        if (i != getComponentIndex()) {
            invalidateColorRect();
        }
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        NamedColorSpace cs = getSourceColorSpace();
        if (cs == null) return;
        double width = getWidth();
        double height = getHeight();
        float vMax = cs.getMaxValue(componentIndex.get());
        float vMin = cs.getMinValue(componentIndex.get());
        switch (getOrientation()) {

            case HORIZONTAL -> {
                thumb.setTranslateX((getComponent() - vMin) * width / (vMax - vMin) - thumb.getWidth() * 0.5
                        + thumbTranslateX.get());
                thumb.setTranslateY((height - thumb.getHeight()) * 0.5
                        + thumbTranslateY.get());
            }
            case VERTICAL -> {
                thumb.setTranslateY(height - (getComponent() - vMin) * height / (vMax - vMin) - thumb.getHeight() * 0.5
                        + thumbTranslateX.get());
                thumb.setTranslateX((width - thumb.getWidth()) * 0.5 + thumbTranslateY.get()
                        + thumbTranslateX.get());
            }
        }
    }

    @Override
    protected AbstractFillTask createFillTask(@NonNull PixelBuffer<IntBuffer> pixelBuffer) {
        if (getDisplayColorSpace() == null || getSourceColorSpace() == null || getTargetColorSpace() == null) {
            return null;
        }
        return new FillTask(new FillTaskRecord(Objects.requireNonNull(pixelBuffer),
                getSourceColorSpace(), getTargetColorSpace(), getDisplayColorSpace(),
                getC0(), getC1(), getC2(), getC3(),
                getComponentIndex(), -1, getRgbFilter() == null ? i -> i : getRgbFilter()),
                getOrientation());
    }


    protected void onMousePressedOrDragged(MouseEvent mouseEvent) {
        float width = (float) getWidth();
        float height = (float) getHeight();
        float x = MathUtil.clamp((float) mouseEvent.getX(), 0, width);
        float y = MathUtil.clamp((float) mouseEvent.getY(), 0, height);
        NamedColorSpace cs = getSourceColorSpace();
        if (cs == null) return;

        float vmax = cs.getMaxValue(componentIndex.get());
        float vmin = cs.getMinValue(componentIndex.get());
        switch (getOrientation()) {
            case HORIZONTAL -> {
                float value = x * (vmax - vmin) / width + vmin;
                setComponent(maybeSnapToTicks(value, getTickUnit()));
            }
            case VERTICAL -> {
                float value = (height - y) * (vmax - vmin) / height + vmin;
                setComponent(maybeSnapToTicks(value, getTickUnit()));
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
                fillHorizontal(tile);
            } else {
                fillVertical(tile);
            }
        }


        public void fillHorizontal(@NonNull TileTask.Tile tile) {
            PixelBuffer<IntBuffer> pixelBuffer = record.pixelBuffer();
            int width = pixelBuffer.getWidth();
            IntBuffer b = pixelBuffer.getBuffer();
            NamedColorSpace scs = record.sourceColorSpace();
            NamedColorSpace tcs = record.targetColorSpace();
            NamedColorSpace dcs = record.displayColorSpace();
            int vIndex = record.xIndex();
            float vMin = scs.getMinValue(vIndex);
            float vMax = scs.getMaxValue(vIndex);
            int yIndex = record.yIndex();
            float invWidth = (vMax - vMin) / (width);
            float[] colorValue = new float[Math.max(4, scs.getNumComponents())];
            colorValue[0] = record.c0();
            colorValue[1] = record.c1();
            colorValue[2] = record.c2();
            colorValue[3] = record.c3();
            float[] sRgb = new float[3];
            float[] tRgb = new float[3];
            float[] dRgb = new float[3];
            float[] scratch = new float[3];
            int[] array = b.array();
            ToIntFunction<Integer> filter = record.rgbFilter();

            int yfrom = tile.yfrom();
            int yto = tile.yto();
            int xfrom = tile.xfrom();
            int xto = tile.xto();

            int xy = yfrom * width;
            for (int x = xfrom; x < xto; x++) {
                float xval = x * invWidth + vMin;
                colorValue[vIndex] = xval;

                int argb = getArgb(scs, tcs, dcs, colorValue, sRgb, tRgb, dRgb, scratch);
                argb = outOfGamut(tRgb) ? 0 : argb;
                array[x + xy] = argb;
            }
            for (int y = yfrom + 1; y < yto; y++) {
                System.arraycopy(array, xy + xfrom, array, y * width + xfrom, xto - xfrom);
            }
        }


        public void fillVertical(@NonNull TileTask.Tile tile) {
            PixelBuffer<IntBuffer> pixelBuffer = record.pixelBuffer();
            int width = pixelBuffer.getWidth();
            int height = pixelBuffer.getHeight();
            IntBuffer b = pixelBuffer.getBuffer();
            NamedColorSpace scs = record.sourceColorSpace();
            NamedColorSpace tcs = record.targetColorSpace();
            NamedColorSpace dcs = record.displayColorSpace();
            int xIndex = record.xIndex();
            float xmin = scs.getMinValue(xIndex);
            float xmax = scs.getMaxValue(xIndex);
            float invHeight = (xmax - xmin) / (height);
            float[] colorValue = new float[Math.max(4, scs.getNumComponents())];
            colorValue[0] = record.c0();
            colorValue[1] = record.c1();
            colorValue[2] = record.c2();
            colorValue[3] = record.c3();
            float[] sRgb = new float[3];
            float[] dRgb = new float[3];
            float[] tRgb = new float[3];
            float[] scratch = new float[3];
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

                int argb = getArgb(scs, tcs, dcs, colorValue, sRgb, tRgb, dRgb, scratch);
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

    public double getTickUnit() {
        return tickUnit.get();
    }

    public @NonNull DoubleProperty tickUnitProperty() {
        return tickUnit;
    }

    public void setTickUnit(double tickUnit) {
        this.tickUnit.set(tickUnit);
    }

}
