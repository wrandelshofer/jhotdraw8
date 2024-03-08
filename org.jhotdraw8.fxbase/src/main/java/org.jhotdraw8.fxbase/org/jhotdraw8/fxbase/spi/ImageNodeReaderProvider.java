/*
 * @(#)ImageNodeReaderProvider.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.spi;

import org.jhotdraw8.annotation.NonNull;

import java.net.URL;

public class ImageNodeReaderProvider implements NodeReaderProvider {
    public ImageNodeReaderProvider() {
    }

    @Override
    public boolean canDecodeInput(@NonNull URL source) {
        return canDecodeInput(source.getFile());
    }

    @Override
    public boolean canDecodeInput(@NonNull String path) {
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
