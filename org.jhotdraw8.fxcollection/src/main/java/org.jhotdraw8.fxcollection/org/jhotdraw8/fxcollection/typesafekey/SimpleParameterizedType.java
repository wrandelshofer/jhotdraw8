package org.jhotdraw8.fxcollection.typesafekey;

import org.jhotdraw8.annotation.NonNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

public class SimpleParameterizedType implements ParameterizedType {
    private final @NonNull Type[] actualTypeArguments;
    private final @NonNull Type rawType;

    public SimpleParameterizedType(@NonNull Type rawType, @NonNull Type... actualTypeArguments) {
        this.rawType = rawType;
        this.actualTypeArguments = actualTypeArguments;
    }

    @Override
    public Type[] getActualTypeArguments() {
        return actualTypeArguments.clone();
    }

    @Override
    public Type getRawType() {
        return rawType;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleParameterizedType)) return false;
        SimpleParameterizedType that = (SimpleParameterizedType) o;
        return Arrays.equals(getActualTypeArguments(), that.getActualTypeArguments()) && Objects.equals(getRawType(), that.getRawType());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getRawType());
        result = 31 * result + Arrays.hashCode(getActualTypeArguments());
        return result;
    }
}
