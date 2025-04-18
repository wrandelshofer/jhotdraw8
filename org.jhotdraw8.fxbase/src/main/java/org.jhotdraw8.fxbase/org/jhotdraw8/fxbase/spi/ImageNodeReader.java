/*
 * @(#)ImageNodeReader.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.spi;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.net.URL;

public class ImageNodeReader implements NodeReader {
    public ImageNodeReader() {
    }

    @Override
    public Node read(URL url) {
        return new ImageView(url.toString());
    }

    @Override
    public Node read(InputStream in) {
        return new ImageView(new Image(in));
    }
}
