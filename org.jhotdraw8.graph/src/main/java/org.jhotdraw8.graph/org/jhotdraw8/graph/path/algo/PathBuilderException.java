/*
 * @(#)PathBuilderException.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path.algo;

import java.io.Serial;

/**
 * PathBuilderException.
 *
 * @author Werner Randelshofer
 */
public class PathBuilderException extends Exception {

    @Serial
    private static final long serialVersionUID = 0L;

    public PathBuilderException(String message) {
        super(message);
    }

    public PathBuilderException(Exception cause) {
        super(cause);
    }

    public PathBuilderException(String message, Exception cause) {
        super(message, cause);
    }

}
