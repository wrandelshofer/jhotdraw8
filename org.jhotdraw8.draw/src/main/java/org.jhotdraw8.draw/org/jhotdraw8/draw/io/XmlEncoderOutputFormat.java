/*
 * @(#)XmlEncoderOutputFormat.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.io;

import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.fxbase.concurrent.WorkState;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.icollection.ChampMap;
import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jspecify.annotations.Nullable;

import java.beans.XMLEncoder;
import java.io.OutputStream;
import java.net.URI;

/**
 * XMLEncoderOutputFormat.
 *
 * @author Werner Randelshofer
 */
public class XmlEncoderOutputFormat implements OutputFormat {
    public static final String XML_SERIALIZER_MIME_TYPE = "application/xml+ser";
    private PersistentMap<Key<?>, Object> options = ChampMap.of();

    public XmlEncoderOutputFormat() {
    }

    @Override
    public void write(OutputStream out, @Nullable URI documentHome, Drawing drawing, WorkState<Void> workState) {
        try (XMLEncoder o = new XMLEncoder(out)) {
            o.writeObject(drawing);
        }
    }

    @Override
    public PersistentMap<Key<?>, Object> getOptions() {
        return options;
    }

    @Override
    public void setOptions(PersistentMap<Key<?>, Object> options) {
        this.options = options;
    }
}
