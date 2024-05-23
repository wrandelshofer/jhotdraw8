/*
 * @(#)ImageNodeReaderProvider.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.spi;


import java.net.URL;

public class ImageNodeReaderProvider implements NodeReaderProvider {
    public ImageNodeReaderProvider() {
    }

    @Override
    public boolean canDecodeInput(URL source) {
        return canDecodeInput(source.getFile());
    }

    @Override
    public boolean canDecodeInput(String path) {
        int p = path.lastIndexOf('.');
        String extension = path.substring(p + 1);
        return switch (extension.toLowerCase()) {
            case "png", "bmp", "gif", "jpg" -> true;
            default -> false;
        };
    }

    @Override
    public NodeReader createReader() {
        return new ImageNodeReader();
    }
}
