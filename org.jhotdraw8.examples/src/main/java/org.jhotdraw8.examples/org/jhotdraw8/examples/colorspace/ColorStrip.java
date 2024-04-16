/*
 * @(#)ColorStrip.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

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
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.NamedColorSpace;
import org.jhotdraw8.color.NamedColorSpaceAdapter;
import org.jhotdraw8.color.SrgbColorSpace;


public class ColorStrip extends HBox {
    private final @NonNull ObjectProperty<NamedColorSpace> colorSpace = new SimpleObjectProperty<>(new NamedColorSpaceAdapter("sRGB", new SrgbColorSpace()));
    private final @NonNull ObjectProperty<float[]> color = new SimpleObjectProperty<>(new float[3]);
    private final @NonNull IntegerProperty component = new SimpleIntegerProperty(0);
    private final @NonNull Canvas canvas = new Canvas();

    @SuppressWarnings("this-escape")
    public ColorStrip() {
        InvalidationListener invalidationListener = c -> update();
        widthProperty().addListener(invalidationListener);
        heightProperty().addListener(invalidationListener);
        colorSpaceProperty().addListener(invalidationListener);
        getChildren().add(canvas);

    }


    public void update() {
        int w = (int) getWidth();
        int h = (int) getHeight();
        canvas.setWidth(w);
        canvas.setHeight(h);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        PixelWriter pw = gc.getPixelWriter();

        NamedColorSpace cs = colorSpace.get();
        int comp = component.get();
        float maxValueCX = cs.getMaxValue(comp);
        float minValueCX = cs.getMinValue(comp);
        float extentCX = maxValueCX - minValueCX;

        float[] colorValue = this.color.get().clone();
        float[] rgb = new float[3];
        for (int x = 0; x < w; x++) {
            float cx = extentCX * x / w + minValueCX;
            colorValue[comp] = cx;
            float[] rgbValue = cs.toRGB(colorValue, rgb);
            Color color = new Color(rgbValue[0], rgbValue[1], rgbValue[2], 1);
            for (int y = 0; y < h; y++) {
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

    public float[] getColor() {
        return color.get();
    }

    public ObjectProperty<float[]> colorProperty() {
        return color;
    }

    public void setColor(float[] color) {
        this.color.set(color);
    }

    public int getComponent() {
        return component.get();
    }

    public IntegerProperty componentProperty() {
        return component;
    }

    public void setComponent(int component) {
        this.component.set(component);
    }
}
