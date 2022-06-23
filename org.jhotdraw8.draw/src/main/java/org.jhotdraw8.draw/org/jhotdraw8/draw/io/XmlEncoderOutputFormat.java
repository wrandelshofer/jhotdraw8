/*
 * @(#)XmlEncoderOutputFormat.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.io;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.facade.ReadOnlyMapFacade;
import org.jhotdraw8.collection.key.Key;
import org.jhotdraw8.collection.readonly.ReadOnlyMap;
import org.jhotdraw8.concurrent.WorkState;
import org.jhotdraw8.draw.figure.Drawing;

import java.beans.XMLEncoder;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.LinkedHashMap;

/**
 * XMLEncoderOutputFormat.
 *
 * @author Werner Randelshofer
 */
public class XmlEncoderOutputFormat implements OutputFormat {
    public static final String XML_SERIALIZER_MIME_TYPE = "application/xml+ser";
    private @NonNull ReadOnlyMap<Key<?>, Object> options = new ReadOnlyMapFacade<>(new LinkedHashMap<>());

    public XmlEncoderOutputFormat() {
    }

    @Override
    public void write(@NonNull OutputStream out, @Nullable URI documentHome, @NonNull Drawing drawing, @NonNull WorkState<Void> workState) throws IOException {
        try (XMLEncoder o = new XMLEncoder(out)) {
            o.writeObject(drawing);
        }
    }

    @NonNull
    @Override
    public ReadOnlyMap<Key<?>, Object> getOptions() {
        return options;
    }

    @Override
    public void setOptions(@NonNull ReadOnlyMap<Key<?>, Object> options) {
        this.options = options;
    }
}
