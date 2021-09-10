/*
 * @(#)XmlEncoderOutputFormat.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.io;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.ReadOnlyOptionsMap;
import org.jhotdraw8.collection.SimpleOptionsMap;
import org.jhotdraw8.concurrent.WorkState;
import org.jhotdraw8.draw.figure.Drawing;

import java.beans.XMLEncoder;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

/**
 * XMLEncoderOutputFormat.
 *
 * @author Werner Randelshofer
 */
public class XmlEncoderOutputFormat implements OutputFormat {
    public static final String XML_SERIALIZER_MIME_TYPE = "application/xml+ser";
    private @NonNull ReadOnlyOptionsMap options = new SimpleOptionsMap();

    public XmlEncoderOutputFormat() {
    }

    @Override
    public void write(@NonNull OutputStream out, URI documentHome, Drawing drawing, WorkState workState) throws IOException {
        try (XMLEncoder o = new XMLEncoder(out)) {
           /* final FigurePersistenceDelegate delegate = new FigurePersistenceDelegate();
            o.setPersistenceDelegate(Figure.class, delegate);
            o.setPersistenceDelegate(Drawing.class, delegate);
            o.setPersistenceDelegate(RectangleFigure.class, delegate);*/
            o.writeObject(drawing);
        }
    }

    @NonNull
    @Override
    public ReadOnlyOptionsMap getOptions() {
        return options;
    }

    @Override
    public void setOptions(ReadOnlyOptionsMap options) {
        this.options = options;
    }
}
