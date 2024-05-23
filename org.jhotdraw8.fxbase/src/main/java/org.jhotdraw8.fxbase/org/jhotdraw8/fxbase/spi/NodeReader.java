/*
 * @(#)NodeReader.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.spi;

import javafx.scene.Node;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

/**
 * Interface for a reader that can read a JavaFX Node from a stream.
 */
public interface NodeReader {
    Node read(URL url) throws IOException;

    default Node read(Path path) throws IOException {
        return read(path.toUri().toURL());
    }

    Node read(InputStream in) throws IOException;
}
