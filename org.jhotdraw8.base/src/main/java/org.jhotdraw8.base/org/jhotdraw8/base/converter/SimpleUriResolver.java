/*
 * @(#)SimpleUriResolver.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.net.UriUtil;

import java.net.URI;

/**
 * Provides utility methods for absolutizing and relativizing URIs.
 *
 * @author Werner Randelshofer
 */
public class SimpleUriResolver implements UriResolver {

    public SimpleUriResolver() {
    }

    @Override
    public @NonNull URI relativize(@Nullable URI base, @NonNull URI uri) {
        return UriUtil.relativize(base, uri);
    }

    @Override
    public @NonNull URI absolutize(@Nullable URI base, @NonNull URI uri) {
        return UriUtil.absolutize(base, uri);
    }

    @Override
    public @NonNull URI getParent(@NonNull URI uri) {
        return UriUtil.getParent(uri);
    }
}
