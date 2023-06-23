/*
 * @(#)IdentityObject.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.impl;

import java.io.Serial;
import java.io.Serializable;

/**
 * An object with a unique identity within this VM.
 */
public class IdentityObject implements Serializable {
    @Serial
    private static final long serialVersionUID = 0L;

    public IdentityObject() {
    }
}
