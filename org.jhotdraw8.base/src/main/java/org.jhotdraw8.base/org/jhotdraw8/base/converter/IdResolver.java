/*
 * @(#)IdResolver.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.net.URI;

public interface IdResolver {
    /**
     * Gets the object for the specified id. Returns null if the id has no
     * object.
     *
     * @param id the id
     * @return the object
     */
    @Nullable Object getObject(@Nullable String id);

    /**
     * Gets the object of the specified class for the specified id.
     * Returns null if the id has no object of this type.
     *
     * @param clazz the clazz
     * @param id    the id
     * @return the object
     */
    default <T> @Nullable T getObject(@NonNull Class<T> clazz, @Nullable String id) {
        Object object = getObject(id);
        if (clazz.isInstance(object)) return clazz.cast(object);
        return null;
    }

    /**
     * Absolutize the given external URI, so that it can be used inside
     * of a drawing (e.g. to access data from the URI).
     * <p>
     * In the internal representation of a drawing, we store all URIs with
     * absolute paths.
     * <p>
     * In the external representation of a drawing, we try to store all URIs
     * relative to the home folder of the document (document home).
     *
     * @param uri an external URI (typically relative to document home)
     * @return an internal URI (typically an absolute path)
     */
    @NonNull URI absolutize(@NonNull URI uri);

}
