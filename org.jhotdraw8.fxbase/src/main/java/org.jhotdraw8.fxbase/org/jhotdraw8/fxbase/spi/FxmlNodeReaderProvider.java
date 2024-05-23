/*
 * @(#)FxmlNodeReaderProvider.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.spi;


import java.net.URL;

public class FxmlNodeReaderProvider implements NodeReaderProvider {
    public FxmlNodeReaderProvider() {
    }

    @Override
    public boolean canDecodeInput(URL source) {
        return canDecodeInput(source.getFile());
    }

    @Override
    public boolean canDecodeInput(String path) {
        return path.toLowerCase().endsWith(".fxml");
    }

    @Override
    public NodeReader createReader() {
        return new FxmlNodeReader();
    }
}
