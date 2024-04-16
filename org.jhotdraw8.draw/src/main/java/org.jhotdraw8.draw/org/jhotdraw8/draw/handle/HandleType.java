/*
 * @(#)HandleType.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.handle;

import org.jhotdraw8.annotation.NonNull;

/**
 * {@code HandleType} is used by tools to request specific handles from figures.
 *
 * @author Werner Randelshofer
 */
public class HandleType {

    /**
     * A Handle of this type should highlight a figure, but should not provide
     * user interaction.
     */
    public static final @NonNull HandleType SELECT = new HandleType();
    /**
     * A Handle of this type should highlight a figure, but should not provide
     * user interaction.
     */
    public static final @NonNull HandleType LEAD = new HandleType();
    /**
     * A Handle of this type should highlight a figure, but should not provide
     * user interaction.
     */
    public static final @NonNull HandleType ANCHOR = new HandleType();
    /**
     * Handles of this type should allow to move (translate) a figure.
     */
    public static final @NonNull HandleType MOVE = new HandleType();
    /**
     * Handle of this type should allow to svgStringReshapeToBuilder (resize) a figure.
     */
    public static final @NonNull HandleType RESIZE = new HandleType();
    /**
     * Handle of this type should allow to transform (scale and rotate) a
     * figure.
     */
    public static final @NonNull HandleType TRANSFORM = new HandleType();
    /**
     * Handle of this type should allow to edit a point of a figure.
     */
    public static final @NonNull HandleType POINT = new HandleType();

    public HandleType() {
    }
}
