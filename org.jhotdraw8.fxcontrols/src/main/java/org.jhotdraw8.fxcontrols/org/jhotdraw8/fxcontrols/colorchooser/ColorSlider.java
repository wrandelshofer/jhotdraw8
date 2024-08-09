/*
 * @(#)ColorSlider.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxcontrols.colorchooser;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.image.PixelBuffer;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.jhotdraw8.base.concurrent.TileTask;
import org.jhotdraw8.color.NamedColorSpace;
import org.jspecify.annotations.Nullable;

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
    @SuppressWarnings("this-escape")
    private final IntegerProperty componentIndex = new SimpleIntegerProperty(this, "componentIndex", 0);

    /**
     * The slider value.
     */
    @SuppressWarnings("this-escape")
    private final FloatProperty value = new SimpleFloatProperty(this, "value", 0);


    @SuppressWarnings("this-escape")
    private final ObjectProperty<Orientation> orientation = new SimpleObjectProperty<>(this, "orientation", Orientation.HORIZONTAL);

    /**
     * The unit distance between tick marks.
     * <p>
     * This must be a double property (and not float) so that we do not run into rounding issues.
     */
    @SuppressWarnings("this-escape")
    private final DoubleProperty minorTickUnit = new SimpleDoubleProperty(this, "minorTickUnit", 1d / 255);
    @SuppressWarnings("this-escape")
    private final DoubleProperty majorTickUnit = new SimpleDoubleProperty(this, "majorTickUnit", 1d / 255);

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
        valueProperty().addListener(o -> this.requestLayout());
    }

    @Override
    protected void onKeyPressed(KeyEvent keyEvent) {
        NamedColorSpace cs = getSourceColorSpace();
        if (cs == null) {
            return;
        }
        final double tickUnit = (keyEvent.isAltDown()) ? getMinorTickUnit() : getMajorTickUnit();
        float v = getValue();
        double vSnappedToTick = Math.round(v / tickUnit) * tickUnit;
        int i = getComponentIndex();
        float vMin = cs.getMinValue(i);
        float vMax = cs.getMaxValue(i);
        switch (keyEvent.getCode()) {
            // increment by tick unit
            case UP, RIGHT -> {
                keyEvent.consume();
                setValue(Math.clamp((float) (vSnappedToTick + tickUnit), vMin, vMax));
            }

            // decrement by tick unit
            case DOWN, LEFT -> {
                keyEvent.consume();
                setValue(Math.clamp((float) (vSnappedToTick - tickUnit), vMin, vMax));
            }

            // snap to tick unit
            case SPACE -> {
                keyEvent.consume();
                setValue(Math.clamp((float) vSnappedToTick, vMin, vMax));
            }
        }
    }

    private void onComponentValueChanged(int i) {
        if (i != getComponentIndex()) {
            invalidate();
        }
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        NamedColorSpace cs = getSourceColorSpace();
        if (cs == null) {
            return;
        }
        double width = getWidth();
        double height = getHeight();
        float vMax = cs.getMaxValue(componentIndex.get());
        float vMin = cs.getMinValue(componentIndex.get());
        switch (getOrientation()) {

            case HORIZONTAL -> {
                thumb.setTranslateX((getValue() - vMin) * width / (vMax - vMin) - thumb.getWidth() * 0.5
                        + thumbTranslateX.get());
                thumb.setTranslateY((height - thumb.getHeight()) * 0.5
                        + thumbTranslateY.get());
            }
            case VERTICAL -> {
                thumb.setTranslateY(height - (getValue() - vMin) * height / (vMax - vMin) - thumb.getHeight() * 0.5
                        + thumbTranslateX.get());
                thumb.setTranslateX((width - thumb.getWidth()) * 0.5 + thumbTranslateY.get()
                        + thumbTranslateX.get());
            }
        }
    }

    @Override
    protected @Nullable AbstractFillTask createFillTask(PixelBuffer<IntBuffer> pixelBuffer) {
        if (getDisplayColorSpace() == null || getSourceColorSpace() == null || getTargetColorSpace() == null) {
            return null;
        }
        return new FillTask(new FillTaskRecord(Objects.requireNonNull(pixelBuffer),
                getSourceColorSpace(), getTargetColorSpace(), getDisplayColorSpace(),
                getC0(), getC1(), getC2(), getC3(),
                getComponentIndex(), -1, 1, getRgbFilter() == null ? i -> i : getRgbFilter()),
                getOrientation());
    }


    protected void onMousePressedOrDragged(MouseEvent mouseEvent) {
        requestFocus();
        float width = (float) getWidth();
        float height = (float) getHeight();
        float x = Math.clamp((float) mouseEvent.getX(), (float) 0, width);
        float y = Math.clamp((float) mouseEvent.getY(), (float) 0, height);
        NamedColorSpace cs = getSourceColorSpace();
        if (cs == null) {
            return;
        }

        float vmax = cs.getMaxValue(componentIndex.get());
        float vmin = cs.getMinValue(componentIndex.get());
        switch (getOrientation()) {
            case HORIZONTAL -> {
                float value = x * (vmax - vmin) / width + vmin;
                setValue(maybeSnapToTicks(value, getMinorTickUnit(), mouseEvent));
            }
            case VERTICAL -> {
                float value = (height - y) * (vmax - vmin) / height + vmin;
                setValue(maybeSnapToTicks(value, getMinorTickUnit(), mouseEvent));
            }
        }
    }


    static class FillTask extends AbstractFillTask {
        private final Orientation orientation;

        public FillTask(FillTaskRecord record, Orientation orientation) {
            super(record);
            this.orientation = orientation;
        }

        public void accept(TileTask.Tile tile) {
            if (orientation == Orientation.HORIZONTAL) {
                fillHorizontal(tile);
            } else {
                fillVertical(tile);
            }
        }


        public void fillHorizontal(TileTask.Tile tile) {
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

                int argb = getArgb(scs, tcs, dcs, colorValue, sRgb, tRgb, dRgb, scratch, 1);
                argb = outOfGamut(tcs, tRgb) ? 0 : argb;
                array[x + xy] = argb;
            }
            for (int y = yfrom + 1; y < yto; y++) {
                System.arraycopy(array, xy + xfrom, array, y * width + xfrom, xto - xfrom);
            }
        }


        public void fillVertical(TileTask.Tile tile) {
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

                int argb = getArgb(scs, tcs, dcs, colorValue, sRgb, tRgb, dRgb, scratch, 1);
                argb = filter.applyAsInt(argb);
                Arrays.fill(array, xy + xfrom, xy + xto, argb);
            }
        }
    }

    public int getComponentIndex() {
        return componentIndex.get();
    }

    public IntegerProperty componentIndexProperty() {
        return componentIndex;
    }

    public void setComponentIndex(int xComponentIndex) {
        this.componentIndex.set(xComponentIndex);
    }

    public Orientation getOrientation() {
        return orientation.get();
    }

    public ObjectProperty<Orientation> orientationProperty() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation.set(orientation);
    }

    public double getMinorTickUnit() {
        return minorTickUnit.get();
    }

    public DoubleProperty minorTickUnitProperty() {
        return minorTickUnit;
    }

    public void setMinorTickUnit(double minorTickUnit) {
        this.minorTickUnit.set(minorTickUnit);
    }

    public float getValue() {
        return value.get();
    }

    public FloatProperty valueProperty() {
        return value;
    }

    public void setValue(float value) {
        this.value.set(value);
    }

    public double getMajorTickUnit() {
        return majorTickUnit.get();
    }

    public DoubleProperty majorTickUnitProperty() {
        return majorTickUnit;
    }

    public void setMajorTickUnit(double majorTickUnit) {
        this.majorTickUnit.set(majorTickUnit);
    }
}

