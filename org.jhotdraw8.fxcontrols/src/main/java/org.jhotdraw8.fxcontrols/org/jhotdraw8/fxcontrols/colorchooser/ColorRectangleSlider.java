package org.jhotdraw8.fxcontrols.colorchooser;

/**
 * Sample Skeleton for 'HueSaturationPane.fxml' Controller Class
 */

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.image.PixelBuffer;
import javafx.scene.input.MouseEvent;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.concurrent.TileTask;
import org.jhotdraw8.base.util.MathUtil;
import org.jhotdraw8.color.NamedColorSpace;

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
    private final @NonNull IntegerProperty xComponentIndex = new SimpleIntegerProperty(this, "xComponentIndex", 1);
    /**
     * The index of the color space component that is displayed along the y-axis of the rectangle.
     */
    private final @NonNull IntegerProperty yComponentIndex = new SimpleIntegerProperty(this, "yComponentIndex", 2);

    /**
     * The unit distance between tick marks on the x-axis.
     */
    private final @NonNull DoubleProperty xTickUnit = new SimpleDoubleProperty(this, "xTickUnit", 1f / 255);
    /**
     * The unit distance between tick marks on the y-axis.
     */
    private final @NonNull DoubleProperty yTickUnit = new SimpleDoubleProperty(this, "yTickUnit", 1f / 255);

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


        xComponentIndex.addListener(o -> invalidateColorRect());
        yComponentIndex.addListener(o -> invalidateColorRect());
    }

    private void onComponentValueChanged(int i) {
        if (i != getXComponentIndex() && i != getYComponentIndex()) {
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
        float xmax = cs.getMaxValue(xComponentIndex.get());
        float xmin = cs.getMinValue(xComponentIndex.get());
        float ymax = cs.getMaxValue(yComponentIndex.get());
        float ymin = cs.getMinValue(yComponentIndex.get());
        thumb.setTranslateX((getXComponent() - xmin) * width / (xmax - xmin) - thumb.getWidth() * 0.5
                + thumbTranslateX.get());
        thumb.setTranslateY(height - (getYComponent() - ymin) * height / (ymax - ymin) - thumb.getHeight() * 0.5
                + thumbTranslateY.get());
    }

    @Override
    protected @Nullable AbstractFillTask createFillTask(@NonNull PixelBuffer<IntBuffer> pixelBuffer) {
        if (getDisplayColorSpace() == null || getSourceColorSpace() == null || getTargetColorSpace() == null)
            return null;
        return new FillTask(new FillTaskRecord(Objects.requireNonNull(pixelBuffer),
                getSourceColorSpace(), getTargetColorSpace(), getDisplayColorSpace(),
                getC0(), getC1(), getC2(), getC3(), getXComponentIndex(), getYComponentIndex(),
                getAlpha(), getRgbFilter() == null ? i -> i : getRgbFilter()));
    }


    protected void onMousePressedOrDragged(MouseEvent mouseEvent) {
        float width = (float) getWidth();
        float height = (float) getHeight();
        float x = MathUtil.clamp((float) mouseEvent.getX(), 0, width);
        float y = MathUtil.clamp((float) mouseEvent.getY(), 0, height);
        NamedColorSpace cs = getSourceColorSpace();
        if (cs == null) return;
        float xmax = cs.getMaxValue(xComponentIndex.get());
        float xmin = cs.getMinValue(xComponentIndex.get());
        float ymax = cs.getMaxValue(yComponentIndex.get());
        float ymin = cs.getMinValue(yComponentIndex.get());
        setXComponent(maybeSnapToTicks(x * (xmax - xmin) / width + xmin, getXTickUnit()));
        setYComponent(maybeSnapToTicks((height - y) * (ymax - ymin) / height + ymin, getYTickUnit()));
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


    static class FillTask extends AbstractFillTask {
        public FillTask(@NonNull FillTaskRecord record) {
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

    public double getXTickUnit() {
        return xTickUnit.get();
    }

    public @NonNull DoubleProperty xTickUnitProperty() {
        return xTickUnit;
    }

    public void setXTickUnit(double xTickUnit) {
        this.xTickUnit.set(xTickUnit);
    }

    public double getYTickUnit() {
        return yTickUnit.get();
    }

    public @NonNull DoubleProperty yTickUnitProperty() {
        return yTickUnit;
    }

    public void setYTickUnit(double yTickUnit) {
        this.yTickUnit.set(yTickUnit);
    }


}

