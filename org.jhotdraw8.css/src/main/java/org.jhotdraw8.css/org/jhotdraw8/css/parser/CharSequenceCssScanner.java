/*
 * @(#)CharSequenceCssScanner.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.css.parser;

import org.jhotdraw8.annotation.NonNull;

public class CharSequenceCssScanner extends AbstractCssScanner {
    private final @NonNull CharSequence seq;

    public CharSequenceCssScanner(@NonNull CharSequence seq) {
        this.seq = seq;
    }

    @Override
    protected int read() {
        return (position < seq.length()) ? seq.charAt((int) position++) : -1;
    }

    @Override
    public void pushBack(int ch) {
        if (ch != -1) {
            position--;
            if (ch == '\n') {
                lineNumber--;
            }
        }
    }
}
