/*
 * @(#)TypeToken.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.reflect;

import org.jhotdraw8.collection.precondition.Preconditions;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * TypeToken captures the type of a generic class
 * including its type parameters.
 * <p>
 * Usage:
 * <pre>
 * {@literal TypeToken<List<Double>> tt = new TypeToken<List<Double>>{};}
 * {@literal Type type = tt.getType();}
 * </pre>
 *
 * @param <T> The type that this type token captures
 */
public class TypeToken<T> {
    private final Type runtimeType;

    public TypeToken() {
        this.runtimeType = capture();
    }

    /**
     * Returns the captured type.
     */
    final Type capture() {
        Type superclass = getClass().getGenericSuperclass();
        Preconditions.checkArgument(superclass instanceof ParameterizedType, "%s isn't parameterized", superclass);
        return ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

    /**
     * Returns the represented type.
     */
    public final Type getType() {
        return runtimeType;
    }
}
