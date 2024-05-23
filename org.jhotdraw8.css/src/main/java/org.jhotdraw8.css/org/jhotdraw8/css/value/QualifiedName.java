/*
 * @(#)QualifiedName.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.value;

import org.jhotdraw8.css.ast.TypeSelector;
import org.jspecify.annotations.Nullable;

import static org.jhotdraw8.css.ast.TypeSelector.ANY_NAMESPACE;

/**
 * Represents a name that is optionally restricted to a specific namespace.
 */
public record QualifiedName(String namespace, String name) implements Comparable<QualifiedName> {
    /**
     * Creates a qualified name
     *
     * @param namespace namespace, if null assigns {@link TypeSelector#ANY_NAMESPACE}
     * @param name      the name
     */
    public QualifiedName(@Nullable String namespace, String name) {
        this.namespace = namespace == null ? ANY_NAMESPACE : namespace;
        this.name = name;
    }


    @Override
    public int compareTo(QualifiedName o) {
        return this.name.compareTo(o.name);
    }

}
