/*
 * @(#)UriResolver.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.converter;

import org.jspecify.annotations.Nullable;

import java.net.URI;

/**
 * Provides utility methods for absolutizing and relativizing URIs.
 *
 * @author Werner Randelshofer
 */
public interface UriResolver {
    URI relativize(@Nullable URI base, URI uri);

    URI absolutize(@Nullable URI base, URI uri);

    URI getParent(URI uri);
}
