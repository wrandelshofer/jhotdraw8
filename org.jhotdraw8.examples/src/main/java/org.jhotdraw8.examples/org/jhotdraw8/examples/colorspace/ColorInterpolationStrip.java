/*
 * @(#)ColorInterpolationStrip.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.examples.colorspace;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.jhotdraw8.color.FXColorInterpolator;
import org.jhotdraw8.color.FXColorUtil;
import org.jhotdraw8.color.NamedColorSpace;
import org.jhotdraw8.color.NamedColorSpaceAdapter;
import org.jhotdraw8.color.SrgbColorSpace;


public class ColorInterpolationStrip extends HBox {
    private final ObjectProperty<NamedColorSpace> colorSpace = new SimpleObjectProperty<>(new NamedColorSpaceAdapter("sRGB", new SrgbColorSpace()));
    private final ObjectProperty<Color> fromColor = new SimpleObjectProperty<>(Color.BLACK);
    private final ObjectProperty<Color> toColor = new SimpleObjectProperty<>(Color.WHITE);
    private final Canvas canvas = new Canvas();

    @SuppressWarnings("this-escape")
    public ColorInterpolationStrip() {
        InvalidationListener invalidationListener = c -> update();
        widthProperty().addListener(invalidationListener);
        heightProperty().addListener(invalidationListener);
        colorSpaceProperty().addListener(invalidationListener);
        fromColorProperty().addListener(invalidationListener);
        toColorProperty().addListener(invalidationListener);
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
        FXColorInterpolator interpolator = new FXColorInterpolator(cs,
                FXColorUtil.fromColor(cs, fromColor.get()),
                FXColorUtil.fromColor(cs, toColor.get()));

        float extent = w - 1;
        for (int x = 0; x < w; x++) {
            float t = x / extent;
            Color color = interpolator.interpolate(t);
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

    public Color getFromColor() {
        return fromColor.get();
    }

    public ObjectProperty<Color> fromColorProperty() {
        return fromColor;
    }

    public void setFromColor(Color fromColor) {
        this.fromColor.set(fromColor);
    }

    public Color getToColor() {
        return toColor.get();
    }

    public ObjectProperty<Color> toColorProperty() {
        return toColor;
    }

    public void setToColor(Color toColor) {
        this.toColor.set(toColor);
    }
}
