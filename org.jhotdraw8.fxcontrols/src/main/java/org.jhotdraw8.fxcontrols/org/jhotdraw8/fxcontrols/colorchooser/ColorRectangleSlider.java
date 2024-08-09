/*
 * @(#)ColorRectangleSlider.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxcontrols.colorchooser;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.image.PixelBuffer;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.jhotdraw8.base.concurrent.TileTask;
import org.jhotdraw8.color.NamedColorSpace;
import org.jspecify.annotations.Nullable;

import java.nio.IntBuffer;
import java.util.Objects;
import java.util.function.ToIntFunction;


/**
 * This slider shows two component dimension of an {@link NamedColorSpace}
 * in a rectangular shape.
 */
public class ColorRectangleSlider extends AbstractColorSlider {
    /**
     * The index of the color space component that is displayed along the x-axis of the rectangle.
     */
    @SuppressWarnings("this-escape")
    private final IntegerProperty xComponentIndex = new SimpleIntegerProperty(this, "xComponentIndex", 1);
    /**
     * The index of the color space component that is displayed along the y-axis of the rectangle.
     */
    @SuppressWarnings("this-escape")
    private final IntegerProperty yComponentIndex = new SimpleIntegerProperty(this, "yComponentIndex", 2);

    /**
     * The minor unit distance between tick marks on the x-axis.
     */
    @SuppressWarnings("this-escape")
    private final DoubleProperty xMinorTickUnit = new SimpleDoubleProperty(this, "xMinorTickUnit", 1f / 255);
    /**
     * The minor unit distance between tick marks on the y-axis.
     */
    @SuppressWarnings("this-escape")
    private final DoubleProperty yMinorTickUnit = new SimpleDoubleProperty(this, "yMinorTickUnit", 1f / 255);
    /**
     * The major unit distance between tick marks on the x-axis.
     */
    @SuppressWarnings("this-escape")
    private final DoubleProperty xMajorTickUnit = new SimpleDoubleProperty(this, "xMajorTickUnit", 1f / 255);
    /**
     * The major unit distance between tick marks on the y-axis.
     */
    @SuppressWarnings("this-escape")
    private final DoubleProperty yMajorTickUnit = new SimpleDoubleProperty(this, "yMajorTickUnit", 1f / 255);

    public ColorRectangleSlider() {
        load();
    }

    @Override
    void initialize() {
        super.initialize();
        c0Property().addListener(o -> this.onComponentValueChanged(0));
        c1Property().addListener(o -> this.onComponentValueChanged(1));
        c2Property().addListener(o -> this.onComponentValueChanged(2));
        c3Property().addListener(o -> this.onComponentValueChanged(3));

        xValue.addListener(o -> requestLayout());
        yValue.addListener(o -> requestLayout());
        xComponentIndex.addListener(o -> invalidate());
        xComponentIndex.addListener(o -> invalidate());
        alpha.addListener(o -> invalidate());

    }

    private void onComponentValueChanged(int i) {
        if (i != getXComponentIndex() && i != getYComponentIndex()) {
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
        float xmax = cs.getMaxValue(xComponentIndex.get());
        float xmin = cs.getMinValue(xComponentIndex.get());
        float ymax = cs.getMaxValue(yComponentIndex.get());
        float ymin = cs.getMinValue(yComponentIndex.get());
        thumb.setTranslateX((getXValue() - xmin) * width / (xmax - xmin) - thumb.getWidth() * 0.5
                + thumbTranslateX.get());
        thumb.setTranslateY(height - (getYValue() - ymin) * height / (ymax - ymin) - thumb.getHeight() * 0.5
                + thumbTranslateY.get());
    }

    @Override
    protected @Nullable AbstractFillTask createFillTask(PixelBuffer<IntBuffer> pixelBuffer) {
        if (getDisplayColorSpace() == null || getSourceColorSpace() == null || getTargetColorSpace() == null) {
            return null;
        }
        return new FillTask(new FillTaskRecord(Objects.requireNonNull(pixelBuffer),
                getSourceColorSpace(), getTargetColorSpace(), getDisplayColorSpace(),
                getC0(), getC1(), getC2(), getC3(), getXComponentIndex(), getYComponentIndex(),
                getAlpha(), getRgbFilter() == null ? i -> i : getRgbFilter()));
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
        float xmax = cs.getMaxValue(xComponentIndex.get());
        float xmin = cs.getMinValue(xComponentIndex.get());
        float ymax = cs.getMaxValue(yComponentIndex.get());
        float ymin = cs.getMinValue(yComponentIndex.get());
        setXValue(maybeSnapToTicks(x * (xmax - xmin) / width + xmin, getXMinorTickUnit(), mouseEvent));
        setYValue(maybeSnapToTicks((height - y) * (ymax - ymin) / height + ymin, getYMinorTickUnit(), mouseEvent));
    }

    /**
     * The slider x-value.
     */
    private final FloatProperty xValue = new SimpleFloatProperty(this, "xValue", 0);
    /**
     * The slider y-value.
     */
    private final FloatProperty yValue = new SimpleFloatProperty(this, "yValue", 0);

    public float getXValue() {
        return xValue.get();
    }

    public FloatProperty xValueProperty() {
        return xValue;
    }

    public void setXValue(float xValue) {
        this.xValue.set(xValue);
    }

    public float getYValue() {
        return yValue.get();
    }

    public FloatProperty yValueProperty() {
        return yValue;
    }

    public void setYValue(float yValue) {
        this.yValue.set(yValue);
    }

    static class FillTask extends AbstractFillTask {
        public FillTask(FillTaskRecord record) {
            super(record);
        }

        public void accept(TileTask.Tile tile) {
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
            int yIndex = record.yIndex();
            float ymin = scs.getMinValue(yIndex);
            float ymax = scs.getMaxValue(yIndex);
            float invWidth = (xmax - xmin) / (width);
            float invHeight = -(ymax - ymin) / (height);
            float[] colorValue = new float[Math.max(4, scs.getNumComponents())];
            colorValue[0] = record.c0();
            colorValue[1] = record.c1();
            colorValue[2] = record.c2();
            colorValue[3] = record.c3();
            float[] sRgb = new float[3];
            float[] tRgb = new float[3];
            float[] dRgb = new float[3];
            float[] pre = new float[3];
            float alpha = record.alpha();
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
                    float yval = y * invHeight + ymax;
                    colorValue[xIndex] = xval;
                    colorValue[yIndex] = yval;

                    int argb = getArgb(scs, tcs, dcs, colorValue, sRgb, tRgb, dRgb, pre, alpha);
                    argb = filter.applyAsInt(argb);
                    array[x + xy] = argb;
                }
            }
        }
    }

    @Override
    protected void onKeyPressed(KeyEvent keyEvent) {
        NamedColorSpace cs = getSourceColorSpace();
        if (cs == null) {
            return;
        }
        final double xTickUnit = (keyEvent.isAltDown()) ? getXMinorTickUnit() : getXMajorTickUnit();
        final double yTickUnit = (keyEvent.isAltDown()) ? getYMinorTickUnit() : getYMajorTickUnit();
        float xValue = getXValue();
        float yValue = getYValue();
        double xSnappedToTick = Math.round(xValue / xTickUnit) * xTickUnit;
        double ySnappedToTick = Math.round(yValue / yTickUnit) * yTickUnit;
        int xIndex = getXComponentIndex();
        float xMin = cs.getMinValue(xIndex);
        float xMax = cs.getMaxValue(xIndex);
        int yIndex = getXComponentIndex();
        float yMin = cs.getMinValue(yIndex);
        float yMax = cs.getMaxValue(yIndex);
        switch (keyEvent.getCode()) {
            // increment by tick unit
            case UP -> {
                keyEvent.consume();
                setYValue(Math.clamp((float) (ySnappedToTick + yTickUnit), yMin, yMax));
            }
            case RIGHT -> {
                keyEvent.consume();
                setXValue(Math.clamp((float) (xSnappedToTick + xTickUnit), xMin, xMax));
            }

            // decrement by tick unit
            case DOWN -> {
                keyEvent.consume();
                setYValue(Math.clamp((float) (ySnappedToTick - yTickUnit), yMin, yMax));
            }
            case LEFT -> {
                keyEvent.consume();
                setXValue(Math.clamp((float) (xSnappedToTick - xTickUnit), xMin, xMax));
            }

            // snap to tick unit
            case SPACE -> {
                keyEvent.consume();
                setYValue(Math.clamp((float) ySnappedToTick, yMin, yMax));
                setXValue(Math.clamp((float) xSnappedToTick, xMin, xMax));
            }
        }
    }


    public int getXComponentIndex() {
        return xComponentIndex.get();
    }

    public IntegerProperty xComponentIndexProperty() {
        return xComponentIndex;
    }

    public void setXComponentIndex(int xComponentIndex) {
        this.xComponentIndex.set(xComponentIndex);
    }

    public int getYComponentIndex() {
        return yComponentIndex.get();
    }

    public IntegerProperty yComponentIndexProperty() {
        return yComponentIndex;
    }

    public void setYComponentIndex(int yComponentIndex) {
        this.yComponentIndex.set(yComponentIndex);
    }

    public double getXMinorTickUnit() {
        return xMinorTickUnit.get();
    }

    public DoubleProperty xMinorTickUnitProperty() {
        return xMinorTickUnit;
    }

    public void setXMinorTickUnit(double xMinorTickUnit) {
        this.xMinorTickUnit.set(xMinorTickUnit);
    }

    public double getYMinorTickUnit() {
        return yMinorTickUnit.get();
    }

    public DoubleProperty yMinorTickUnitProperty() {
        return yMinorTickUnit;
    }

    public void setYMinorTickUnit(double yMinorTickUnit) {
        this.yMinorTickUnit.set(yMinorTickUnit);
    }

    public double getXMajorTickUnit() {
        return xMajorTickUnit.get();
    }

    public DoubleProperty xMajorTickUnitProperty() {
        return xMajorTickUnit;
    }

    public void setXMajorTickUnit(double xMajorTickUnit) {
        this.xMajorTickUnit.set(xMajorTickUnit);
    }

    public double getYMajorTickUnit() {
        return yMajorTickUnit.get();
    }

    public DoubleProperty yMajorTickUnitProperty() {
        return yMajorTickUnit;
    }

    public void setYMajorTickUnit(double yMajorTickUnit) {
        this.yMajorTickUnit.set(yMajorTickUnit);
    }
}

