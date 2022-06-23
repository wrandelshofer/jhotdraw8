/*
 * @(#)ReaderCssScanner.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css;

import org.jhotdraw8.collection.primitive.IntArrayList;

import java.io.IOException;
import java.io.Reader;

public class ReaderCssScanner extends AbstractCssScanner {

    /**
     * The underlying reader.
     */
    private final Reader in;


    /**
     * Stack of pushed back characters.
     */
    private final IntArrayList pushedChars = new IntArrayList();

    public ReaderCssScanner(Reader reader) {
        this.in = reader;
    }

    @Override
    protected int read() throws IOException {
        if (!pushedChars.isEmpty()) {
            currentChar = pushedChars.removeAtAsInt(pushedChars.size() - 1);
            position++;
            return currentChar;
        }

        int ch = in.read();
        if (ch != -1) {
            position++;
        }
        return ch;
    }

    @Override
    public void pushBack(int ch) {
        if (ch != -1) {
            position--;
            if (ch == '\n') {
                lineNumber--;
            }
            pushedChars.addAsInt(ch);
        }
    }

    @Override
    public long getPosition() {
        return position;
    }
}
