/*
 * @(#)AbstractCssScanner.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.css.parser;

import java.io.IOException;

public abstract class AbstractCssScanner implements CssScanner {
    /**
     * The current position in the input stream.
     */
    protected long position;
    /**
     * The current line number in the input stream.
     * An input stream starts with line number 1.
     */
    protected long lineNumber = 1;

    /**
     * The current character.
     */
    protected int currentChar;

    /**
     * Whether we need to skip a linefeed on the next read.
     */
    protected boolean skipLF;

    public AbstractCssScanner() {
    }

    @Override
    public int currentChar() {
        return currentChar;
    }

    @Override
    public long getLineNumber() {
        return lineNumber;
    }

    protected abstract int read() throws IOException;

    @Override
    public int nextChar() throws IOException {
        currentChar = read();
        if (skipLF && currentChar == '\n') {
            skipLF = false;
            currentChar = read();
        }

        switch (currentChar) {
        case -1: // EOF
            break;
        case '\r': // translate "\r", "\r\n" into "\n"
            skipLF = true;
            currentChar = '\n';
            lineNumber++;
            break;
        case '\f': // translate "\f" into "\n"
            currentChar = '\n';
            lineNumber++;
            break;
        case '\n':
            lineNumber++;
            break;
        case '\000':
            currentChar = '\ufffd';
            break;
        default:
            break;
        }

        return currentChar;
    }

    @Override
    public long getPosition() {
        return position;
    }
}
