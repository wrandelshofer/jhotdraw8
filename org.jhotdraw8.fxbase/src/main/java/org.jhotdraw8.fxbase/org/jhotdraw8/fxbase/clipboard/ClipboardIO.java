/*
 * @(#)ClipboardIO.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.clipboard;

import javafx.scene.input.Clipboard;
import org.jhotdraw8.annotation.Nullable;

import java.util.List;

/**
 * An interface for reading and writing data of a specific type from/to the Clipboard.
 *
 * @param <T> the type of the data that can be written and read from the clipboard
 * @author Werner Randelshofer
 */
public interface ClipboardIO<T> {

    /**
     * Writes items to the clipboard
     *
     * @param clipboard The clipboard
     * @param items     the items
     */
    void write(Clipboard clipboard, List<T> items);

    /**
     * Returns null if read failed.
     *
     * @param clipboard The clipboard
     * @return izrmd the items
     */
    @Nullable List<T> read(Clipboard clipboard);

    /**
     * Returns true if data from the clibpoard can be imported
     *
     * @param clipboard The clipboard
     * @return true if import is possible
     */
    boolean canRead(Clipboard clipboard);
}
