/*
 * @(#)UriResolver.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.net.URI;

/**
 * Provides utility methods for absolutizing and relativizing URIs.
 *
 * @author Werner Randelshofer
 */
public interface UriResolver {
    @NonNull URI relativize(@Nullable URI base, @NonNull URI uri);

    @NonNull URI absolutize(@Nullable URI base, @NonNull URI uri);

    @NonNull URI getParent(@NonNull URI uri);
}
