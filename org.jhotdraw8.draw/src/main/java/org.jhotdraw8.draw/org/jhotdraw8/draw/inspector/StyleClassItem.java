/*
 * @(#)StyleClassItem.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.inspector;

import org.jhotdraw8.annotation.NonNull;

/**
 * StyleClassItem.
 *
 * @author Werner Randelshofer
 */
public class StyleClassItem {

    /**
     * The text of the tag.
     */
    private final @NonNull String text;
    /**
     * Whether the tag is present in all elements.
     */
    private final boolean inAllElements;

    public StyleClassItem(String text, boolean isInAllElements) {
        this.text = text;
        this.inAllElements = isInAllElements;
    }

    public String getText() {
        return text;
    }

    public boolean isInAllElements() {
        return inAllElements;
    }

}
