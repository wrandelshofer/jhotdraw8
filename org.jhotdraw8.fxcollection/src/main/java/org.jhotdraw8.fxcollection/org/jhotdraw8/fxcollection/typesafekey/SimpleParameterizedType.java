package org.jhotdraw8.fxcollection.typesafekey;

import org.jhotdraw8.annotation.NonNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

public record SimpleParameterizedType(@NonNull Type rawType,
                                      @NonNull Type... actualTypeArguments) implements ParameterizedType {

    public Type getTypeArgument(int i) {
        return actualTypeArguments[i];
    }

    public int getTypeArgumentCount() {
        return actualTypeArguments.length;
    }

    @Override
    public String getTypeName() {
        StringBuilder b = new StringBuilder();
        b.append(rawType.getTypeName());
        b.append('<');
        int first = b.length();
        for (Type t : actualTypeArguments) {
            if (b.length() != first) b.append(',');
            b.append(t.getTypeName());
        }
        b.append('>');
        return b.toString();
    }

    @Override
    public Type[] getActualTypeArguments() {
        return actualTypeArguments.clone();
    }

    @Override
    public @NonNull Type getRawType() {
        return rawType;
    }

    @Override
    public Type getOwnerType() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleParameterizedType that = (SimpleParameterizedType) o;
        return Objects.equals(rawType, that.rawType) && Arrays.equals(actualTypeArguments, that.actualTypeArguments);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(rawType);
        result = 31 * result + Arrays.hashCode(actualTypeArguments);
        return result;
    }
}
