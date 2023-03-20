/*
 * @(#)SvgNodeReader.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.svg.spi;

import javafx.scene.Node;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.fxbase.spi.NodeReader;
import org.jhotdraw8.svg.io.FXSvgTinyReader;

import javax.xml.transform.stream.StreamSource;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class SvgNodeReader implements NodeReader {
    public SvgNodeReader() {
    }

    @Override
    public Node read(@NonNull URL url) throws IOException {
        try (InputStream in = new BufferedInputStream(url.openStream())) {
            return new FXSvgTinyReader().read(new StreamSource(in));
        }
    }

    @Override
    public Node read(@NonNull InputStream in) throws IOException {
        return new FXSvgTinyReader().read(new StreamSource(in));
    }
}
