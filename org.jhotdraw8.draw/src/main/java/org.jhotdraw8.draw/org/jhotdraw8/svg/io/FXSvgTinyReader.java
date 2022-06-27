/*
 * @(#)FXSvgTinyReader.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.svg.io;

import javafx.scene.Node;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.typesafekey.MapAccessor;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.draw.render.RenderingIntent;
import org.jhotdraw8.draw.render.SimpleDrawingRenderer;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reads an SVG "Tiny" 1.2 file and creates JavaFX nodes from it.
 */
public class FXSvgTinyReader {
    public FXSvgTinyReader() {
    }

    public Node read(@NonNull Path file) throws IOException {
        try (final InputStream in = new BufferedInputStream(Files.newInputStream(file))) {
            return read(new StreamSource(in));
        }
    }

    public Node read(@NonNull URL file) throws IOException {
        try (final InputStream in = new BufferedInputStream(file.openStream())) {
            return read(new StreamSource(in));
        }
    }

    /**
     * The reader does not close the provided source.
     */
    public Node read(@NonNull Source in) throws IOException {
        Map<MapAccessor<?>, Object> m = new LinkedHashMap<>();
        RenderContext.RENDERING_INTENT.put(m, RenderingIntent.EXPORT);
        return read(in, m);
    }

    /**
     * The readers does not close the provide source.
     */
    @SuppressWarnings("unchecked")
    public Node read(@NonNull Source in, Map<MapAccessor<?>, Object> renderingHints) throws IOException {
        Figure figure = new FigureSvgTinyReader().read(in);
        SimpleDrawingRenderer r = new SimpleDrawingRenderer();
        renderingHints.forEach((key, value) -> r.put((MapAccessor<Object>) key, value));
        Node node = r.render(figure);
        node.setManaged(true);
        return node;
    }
}
