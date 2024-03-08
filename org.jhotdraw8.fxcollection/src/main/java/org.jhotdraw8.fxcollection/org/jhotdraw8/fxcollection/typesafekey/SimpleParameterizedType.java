package org.jhotdraw8.fxcollection.typesafekey;

import org.jhotdraw8.annotation.NonNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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
}
