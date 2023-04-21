/*
 * @(#)DataFormats.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.clipboard;

import javafx.scene.input.DataFormat;

public class DataFormats {
    private DataFormats() {
    }

    public static DataFormat registerDataFormat(String mimeType) {
        DataFormat fmt = DataFormat.lookupMimeType(mimeType);
        if (fmt == null) {
            fmt = new DataFormat(mimeType);
        }
        return fmt;
    }

}
