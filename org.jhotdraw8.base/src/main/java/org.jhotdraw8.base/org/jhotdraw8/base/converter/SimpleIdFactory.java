/*
 * @(#)SimpleIdFactory.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * SimpleIdFactory.
 *
 * @author Werner Randelshofer
 */
public class SimpleIdFactory implements IdFactory {
    private final @NonNull Map<String, Long> prefixToNextId = new HashMap<>(128, 0.4f);
    private final @NonNull Map<String, Object> idToObject = new HashMap<>(128, 0.4f);
    private final @NonNull Map<Object, String> objectToId = new HashMap<>(128, 0.4f);

    private @NonNull UriResolver uriResolver = new SimpleUriResolver();

    public SimpleIdFactory() {
    }

    @Override
    public void reset() {
        prefixToNextId.clear();
        idToObject.clear();
        objectToId.clear();
    }

    @Override
    public @Nullable String createId(Object object) {
        return createId(object, "");
    }

    @Override
    public String getId(Object object) {
        return objectToId.get(object);
    }

    private @Nullable URI documentHome;

    @Override
    public void setDocumentHome(@Nullable URI documentHome) {
        this.documentHome = documentHome;
    }

    @Override
    public @NonNull URI relativize(@NonNull URI uri) {
        return documentHome == null ? uri : uriResolver.relativize(documentHome, uri);
    }

    @Override
    public Object getObject(String id) {
        return idToObject.get(id);
    }

    @Override
    public @NonNull URI absolutize(@NonNull URI uri) {
        return documentHome == null ? uri : uriResolver.absolutize(documentHome, uri);
    }

    @Override
    public Object putIdAndObject(@NonNull String id, @NonNull Object object) {
        String oldId = objectToId.put(object, id);
        if (oldId != null) {
            idToObject.remove(oldId);
        }
        Object oldObject = idToObject.put(id, object);
        if (oldObject != null) {
            objectToId.remove(oldObject);
        }
        return oldObject;
    }

    @Override
    public Object putIdToObject(@NonNull String id, @NonNull Object object) {
        Object oldObject = idToObject.put(id, object);
        if (oldObject != null) {
            objectToId.remove(oldObject);
        }
        return oldObject;
    }

    @Override
    public String createId(Object object, @Nullable String prefix) {
        String id = objectToId.get(object);
        if (id == null) {
            long pNextId = prefixToNextId.getOrDefault(prefix, 1L);

            do { // XXX linear search
                id = (prefix == null ? "" : prefix) + pNextId++;
            } while (idToObject.containsKey(id));
            objectToId.put(object, id);
            idToObject.put(id, object);
            prefixToNextId.put(prefix, pNextId);
        }
        return id;
    }

    @Override
    public @Nullable String createId(@NonNull Object object, @Nullable String prefix, @Nullable String suggestedId) {
        String existingId = objectToId.get(object);
        if (existingId == null) {
            if (suggestedId != null && !idToObject.containsKey(suggestedId)) {
                existingId = suggestedId;
            } else {
                long pNextId = prefixToNextId.getOrDefault(prefix, 1L);

                do { // XXX linear search
                    existingId = (prefix == null ? "" : prefix) + pNextId++;
                } while (idToObject.containsKey(existingId));
                prefixToNextId.put(prefix, pNextId);
            }
            objectToId.put(object, existingId);
            idToObject.put(existingId, object);
        } else {

        }
        return existingId;
    }

    public @NonNull UriResolver getUriResolver() {
        return uriResolver;
    }

    public void setUriResolver(@NonNull UriResolver uriResolver) {
        this.uriResolver = uriResolver;
    }
}
