/*
 * @(#)XmlEncoderOutputFormat.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.io;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.fxbase.concurrent.WorkState;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.icollection.ChampMap;
import org.jhotdraw8.icollection.immutable.ImmutableMap;

import java.beans.XMLEncoder;
import java.io.OutputStream;
import java.net.URI;

/**
 * XMLEncoderOutputFormat.
 *
 * @author Werner Randelshofer
 */
public class XmlEncoderOutputFormat implements OutputFormat {
    public static final @NonNull String XML_SERIALIZER_MIME_TYPE = "application/xml+ser";
    private @NonNull ImmutableMap<Key<?>, Object> options = ChampMap.of();

    public XmlEncoderOutputFormat() {
    }

    @Override
    public void write(@NonNull OutputStream out, @Nullable URI documentHome, @NonNull Drawing drawing, @NonNull WorkState<Void> workState) {
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
