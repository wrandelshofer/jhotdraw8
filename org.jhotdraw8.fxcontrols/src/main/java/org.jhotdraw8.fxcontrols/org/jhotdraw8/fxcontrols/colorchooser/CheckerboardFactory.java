/*
 * @(#)CheckerboardFactory.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxcontrols.colorchooser;

import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.ImagePattern;

import java.nio.IntBuffer;
import java.util.Arrays;

class CheckerboardFactory {
    /**
     * Don't let anyone instantiate this class.
     */
    private CheckerboardFactory() {
    }

    /**
     * Creates a checkerboard image pattern.
     *
     * @param size     size of a checkerboard tile
     * @param evenArgb color for even tiles
     * @param oddArgb  color for odd tiles
     * @return the image pattern
     */
    public static ImagePattern createCheckerboardPattern(int size, int evenArgb, int oddArgb) {
        int width = size * 2;
        int height = width;
        PixelFormat<IntBuffer> pixelFormat = PixelFormat.getIntArgbPreInstance();
        IntBuffer intBuffer = IntBuffer.allocate(width * height);
        var pixelBuffer = new PixelBuffer<>(width, height, intBuffer, pixelFormat);
        WritableImage image = new WritableImage(pixelBuffer);
        int[] a = intBuffer.array();

        // fill first even line
        Arrays.fill(a, 0, size, evenArgb);
        Arrays.fill(a, size, size * 2, oddArgb);

        // fill first odd line
        int xy = size * width;
        Arrays.fill(a, xy, xy + size, oddArgb);
        Arrays.fill(a, xy + size, xy + size * 2, evenArgb);

        for (int y = 1; y < size; y++) {
            xy = width * y;
            System.arraycopy(a, 0, a, xy, width);
            System.arraycopy(a, size * width, a, xy + size * width, width);
        }

        return new ImagePattern(image, 0, 0, width, height, false);
    }
}
