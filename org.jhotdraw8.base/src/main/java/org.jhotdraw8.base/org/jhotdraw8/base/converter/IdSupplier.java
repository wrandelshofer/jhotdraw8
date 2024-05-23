/*
 * @(#)IdSupplier.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.converter;

import org.jspecify.annotations.Nullable;

import java.net.URI;

public interface IdSupplier {
    /**
     * Gets an id for the specified object. Returns null if the object has no
     * id.
     *
     * @param object the object
     * @return the id
     */
    @Nullable String getId(Object object);

    /**
     * Relativize the given URI, so that it can be used for storage in a
     * file.
     * <p>
     * In the internal representation of a drawing, we store all URIs with
     * absolute paths.
     * <p>
     * In the external representation of a drawing, we try to store all URIs
     * relative to the home folder of the document (document home).
     *
     * @param uri an internal URI (typically an absolute path)
     * @return an external URI (typically relative to document home)
     */
    URI relativize(URI uri);

}
