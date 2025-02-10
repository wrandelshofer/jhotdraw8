/*
 * @(#)CharSequenceReader.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.io;


import java.io.Reader;

/**
 * CharSequenceReader.
 *
 */
public class CharSequenceReader extends Reader {

    private final CharSequence buf;
    private int pos;

    public CharSequenceReader(CharSequence buf) {
        this.buf = buf;
    }

    @Override
    public int read() {
        if (buf.length() <= pos) {
            return -1;
        }
        return buf.charAt(pos++);
    }

    @Override
    public int read(char[] cbuf, int off, int len) {
        len = Math.min(len, buf.length() - pos);
        for (int i = 0; i < len; i++) {
            cbuf[i] = buf.charAt(pos++);
        }
        return len;
    }

    @Override
    public void close() {
        // empty
    }

}
