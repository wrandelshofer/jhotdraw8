package org.jhotdraw8.examples.colorspace;

import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.jhotdraw8.color.NamedColorSpace;
import org.jhotdraw8.color.NamedColorSpaceAdapter;

import java.awt.color.ColorSpace;

import static org.jhotdraw8.base.util.MathUtil.clamp;

public class ColorRect extends HBox {
    private final ObjectProperty<NamedColorSpace> colorSpace = new SimpleObjectProperty<>(new NamedColorSpaceAdapter("sRGB", ColorSpace.getInstance(ColorSpace.CS_sRGB)));
    private final ObjectProperty<float[]> baseColor = new SimpleObjectProperty<>(new float[3]);
    private final IntegerProperty xComponent = new SimpleIntegerProperty(0);
    private final IntegerProperty yComponent = new SimpleIntegerProperty(1);
    private final Canvas canvas = new Canvas();

    public ColorRect() {
        InvalidationListener invalidationListener = c -> update();
        widthProperty().addListener(invalidationListener);
        heightProperty().addListener(invalidationListener);
        colorSpaceProperty().addListener(invalidationListener);
        baseColorProperty().addListener(invalidationListener);
        layoutBoundsProperty().addListener(invalidationListener);
        getChildren().add(canvas);
        setMinWidth(10);
        setMinHeight(10);
    }


    public void update() {

        int w = (int) getWidth();
        int h = (int) getHeight();
        canvas.setWidth(w);
        canvas.setHeight(h);


        GraphicsContext gc = canvas.getGraphicsContext2D();
        PixelWriter pw = gc.getPixelWriter();

        NamedColorSpace cs = colorSpace.get();
        if (cs == null) {
            return;
        }
        int xComp = xComponent.get();
        int yComp = yComponent.get();
        float maxValueCX = cs.getMaxValue(xComp);
        float minValueCX = cs.getMinValue(xComp);
        float extentCX = maxValueCX - minValueCX;
        float maxValueCY = cs.getMaxValue(yComp);
        float minValueCY = cs.getMinValue(yComp);
        float extentCY = maxValueCY - minValueCY;

        float[] colorValue = this.baseColor.get().clone();
        float[] rgb = new float[3];
        for (int x = 0; x < w; x++) {
            float cx = extentCX * x / w + minValueCX;
            colorValue[xComp] = cx;
            for (int y = 0; y < h; y++) {
                float cy = extentCY * y / h + minValueCY;
                colorValue[yComp] = cy;
                float[] rgbValue = cs.toRGB(colorValue, rgb);
                Color color = new Color(clamp(rgbValue[0], 0, 1),
                        clamp(rgbValue[1], 0, 1), clamp(rgbValue[2], 0, 1), 1);
                pw.setColor(x, y, color);
            }
        }

    }

    public NamedColorSpace getColorSpace() {
        return colorSpace.get();
    }

    public ObjectProperty<NamedColorSpace> colorSpaceProperty() {
        return colorSpace;
    }

    public void setColorSpace(NamedColorSpace colorSpace) {
        this.colorSpace.set(colorSpace);
    }

    public float[] getBaseColor() {
        return baseColor.get();
    }

    public ObjectProperty<float[]> baseColorProperty() {
        return baseColor;
    }

    public void setBaseColor(float[] baseColor) {
        this.baseColor.set(baseColor);
    }

    public int getXComponent() {
        return xComponent.get();
    }

    public IntegerProperty xComponentProperty() {
        return xComponent;
    }

    public void setXComponent(int xComponent) {
        this.xComponent.set(xComponent);
    }

    public int getYComponent() {
        return yComponent.get();
    }

    public IntegerProperty yComponentProperty() {
        return yComponent;
    }

    public void setYComponent(int xComponent) {
        this.yComponent.set(xComponent);
    }
}
