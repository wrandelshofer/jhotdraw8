/*
 * @(#)CharBufferReader.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.io;


import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

/**
 * CharBufferReader.
 *
 */
public class CharBufferReader extends Reader {

    private final CharBuffer buf;

    public CharBufferReader(CharBuffer buf) {
        this.buf = buf;
    }

    @Override
    public int read() {
        if (buf.remaining() <= 0) {
            return -1;
        }
        return buf.get();
    }

    @Override
    public int read(CharBuffer target) throws IOException {
        return buf.read(target);
    }

    @Override
    public int read(char[] cbuf, int off, int len) {
        len = Math.min(len, buf.remaining());
        buf.get(cbuf, off, len);
        return len;
    }

    @Override
    public void close() {
        // empty
    }

}
