/*
 * @(#)SvgImageReaderProvider.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.svg.spi;

import org.jhotdraw8.fxbase.spi.NodeReader;
import org.jhotdraw8.fxbase.spi.NodeReaderProvider;

import java.net.URL;

public class SvgImageReaderProvider implements NodeReaderProvider {
    public SvgImageReaderProvider() {
    }

    @Override
    public boolean canDecodeInput(URL source) {
        return canDecodeInput(source.getFile());
    }

    @Override
    public boolean canDecodeInput(String path) {
        return path.toLowerCase().endsWith(".svg");
    }

    @Override
    public NodeReader createReader() {
        return new SvgNodeReader();
    }
}
