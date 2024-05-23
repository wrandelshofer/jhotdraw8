/*
 * @(#)SimpleUriResolver.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.converter;

import org.jhotdraw8.base.net.UriUtil;
import org.jspecify.annotations.Nullable;

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
    public URI relativize(@Nullable URI base, URI uri) {
        return UriUtil.relativize(base, uri);
    }

    @Override
    public URI absolutize(@Nullable URI base, URI uri) {
        return UriUtil.absolutize(base, uri);
    }

    @Override
    public URI getParent(URI uri) {
        return UriUtil.getParent(uri);
    }
}
