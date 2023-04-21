/*
 * @(#)XmlEncoderOutputFormat.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.io;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.champ.ChampMap;
import org.jhotdraw8.collection.immutable.ImmutableMap;
import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.fxbase.concurrent.WorkState;
import org.jhotdraw8.fxcollection.typesafekey.Key;

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
    private @NonNull ImmutableMap<Key<?>, Object> options = ChampMap.of();

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
    public ImmutableMap<Key<?>, Object> getOptions() {
        return options;
    }

    @Override
    public void setOptions(@NonNull ImmutableMap<Key<?>, Object> options) {
        this.options = options;
    }
}
