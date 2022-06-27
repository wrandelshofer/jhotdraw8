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
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reads an SVG "Tiny" 1.2 file and creates JavaFX nodes from it.
 */
public class FXSvgTinyReader {
    public FXSvgTinyReader() {
    }

    public Node read(@NonNull Source in) throws IOException {
        Map<MapAccessor<?>, Object> m = new LinkedHashMap<>();
        RenderContext.RENDERING_INTENT.put(m, RenderingIntent.EXPORT);
        return read(in, m);
    }

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
