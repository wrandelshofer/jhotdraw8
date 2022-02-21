/*
 * @(#)QualifiedName.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Objects;

import static org.jhotdraw8.css.SelectorModel.ANY_NAMESPACE;

/**
 * Represents a name that is optionally restricted to a specific namespace.
 */
public class QualifiedName implements Comparable<QualifiedName> {
    private final @NonNull String namespace;
    private final @NonNull String name;

    /**
     * Creates a qualified name
     *
     * @param namespace namespace, if null assigns {@link org.jhotdraw8.css.SelectorModel#ANY_NAMESPACE}
     * @param name      the name
     */
    public QualifiedName(@Nullable String namespace, @NonNull String name) {
        this.namespace = namespace == null ? ANY_NAMESPACE : namespace;
        this.name = name;
    }

    public @NonNull String getNamespace() {
        return namespace;
    }

    public @NonNull String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof QualifiedName)) {
            return false;
        }
        QualifiedName that = (QualifiedName) o;
        return Objects.equals(namespace, that.namespace) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, name);
    }

    @Override
    public int compareTo(@NonNull QualifiedName o) {
        return this.name.compareTo(o.name);
    }

    @Override
    public @NonNull String toString() {
        return "QualifiedName{" +
                "namespace='" + namespace + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
